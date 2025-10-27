package com.rideshare.userservice.service;

import com.rideshare.userservice.dto.ApiResponse;
import com.rideshare.userservice.dto.RegistrationRequest;
import com.rideshare.userservice.dto.UserDto;
import com.rideshare.userservice.entity.Role;
import com.rideshare.userservice.entity.User;
import com.rideshare.userservice.exception.UserNotFoundException;
import com.rideshare.userservice.repository.RoleRepository;
import com.rideshare.userservice.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private Role riderRole;
    private Role bothRole;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        riderRole = new Role(1L, "RIDER", "Rider");
        bothRole = new Role(3L, "BOTH", "Both");

        user = new User();
        user.setFirstName("Gopal");
        user.setLastName("Rao");
        user.setMobileNumber("9885791402");

        userDto = new UserDto();
        userDto.setFirstName("Gopal");
        userDto.setLastName("Rao");
        userDto.setMobileNumber("9885791402");
    }

    // 1️⃣ Test: New user registration (RIDER)
    @Test
    void testRegisterUser_NewRider() {
        RegistrationRequest request = new RegistrationRequest("John", "Doe", "M", "9999999999", "RIDER");

        when(userRepository.findByMobileNumber("9999999999")).thenReturn(Optional.empty());
        when(roleRepository.findByName("RIDER")).thenReturn(Optional.of(riderRole));

        User mappedUser = new User();
        mappedUser.setFirstName("John");
        mappedUser.setMobileNumber("9999999999");
        mappedUser.setRole(riderRole);

        //when(modelMapper.map(any(RegistrationRequest.class), eq(User.class))).thenReturn(mappedUser);
        when(userRepository.save(any(User.class))).thenReturn(mappedUser);

        ApiResponse response = userService.registerUser(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("Registration successful");
        verify(userRepository, times(1)).save(any(User.class));
    }

    // 2️⃣ Test: Existing user upgrading from RIDER → BOTH
    @Test
    void testRegisterUser_UpgradeToBoth() {
        RegistrationRequest request = new RegistrationRequest("John", "Doe", "M", "9999999999", "OWNER");

        User existingUser = new User();
        existingUser.setMobileNumber("9999999999");
        existingUser.setRole(riderRole);

        when(userRepository.findByMobileNumber("9999999999")).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName("BOTH")).thenReturn(Optional.of(bothRole));

        ApiResponse response = userService.registerUser(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("BOTH");
        verify(userRepository, times(1)).save(existingUser);
    }

    // 3️⃣ Test: Existing user already has same role
    @Test
    void testRegisterUser_AlreadySameRole() {
        RegistrationRequest request = new RegistrationRequest("John", "Doe", "M", "9999999999", "RIDER");

        User existingUser = new User();
        existingUser.setMobileNumber("9999999999");
        existingUser.setRole(riderRole);

        when(userRepository.findByMobileNumber("9999999999")).thenReturn(Optional.of(existingUser));

        ApiResponse response = userService.registerUser(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("already registered");
        verify(userRepository, never()).save(any());
    }

    // 4️⃣ Test: Role not found scenario
    @Test
    void testRegisterUser_RoleNotFound() {
        RegistrationRequest request = new RegistrationRequest("John", "Doe", "M", "8888888888", "ADMIN");

        when(userRepository.findByMobileNumber("8888888888")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        try {
            userService.registerUser(request);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("Role not found");
        }

        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_ExistingUserAlreadyBoth() {
        User existingUser = new User();
        existingUser.setMobileNumber("9999999999");

        Role roleBoth = new Role();
        roleBoth.setName("BOTH");
        existingUser.setRole(roleBoth);

        when(userRepository.findByMobileNumber("9999999999")).thenReturn(Optional.of(existingUser));

        RegistrationRequest req = new RegistrationRequest("John", "Doe", "M", "9999999999", "OWNER");
        ApiResponse response = userService.registerUser(req);

        Assertions.assertFalse(response.isSuccess());
        Assertions.assertTrue(response.getMessage().contains("already have BOTH roles"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUser_ExistingUserSameRole() {
        User existingUser = new User();
        existingUser.setMobileNumber("7777777777");

        Role roleRider = new Role();
        roleRider.setName("RIDER");
        existingUser.setRole(roleRider);

        when(userRepository.findByMobileNumber("7777777777")).thenReturn(Optional.of(existingUser));

        RegistrationRequest req = new RegistrationRequest("John", "Doe", "M", "7777777777", "RIDER");
        ApiResponse response = userService.registerUser(req);

        Assertions.assertFalse(response.isSuccess());
        Assertions.assertTrue(response.getMessage().contains("already registered with this role"));
        verify(userRepository, never()).save(any());
    }
    @Test
    void testRegisterUser_BothRoleNotFound_ShouldThrowException() {
        // Arrange
        User existingUser = new User();
        existingUser.setMobileNumber("9999999999");

        Role roleRider = new Role();
        roleRider.setName("RIDER");
        existingUser.setRole(roleRider);

        when(userRepository.findByMobileNumber("9999999999")).thenReturn(Optional.of(existingUser));

        // Simulate no BOTH role found
        when(roleRepository.findByName("BOTH")).thenReturn(Optional.empty());

        RegistrationRequest req = new RegistrationRequest("John", "Doe", "M", "9999999999", "OWNER");

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(req);
        });

        assertEquals("Role BOTH not found", thrown.getMessage());
    }

    // ✅ Test for getAllUsers()
    @Test
    void testGetAllUsers_ShouldReturnListOfUserDtos() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Use doReturn to avoid PotentialStubbingProblem
        doReturn(List.of(userDto))
                .when(modelMapper)
                .map(anyList(), any(Type.class));

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Gopal", result.get(0).getFirstName());
        verify(userRepository, times(1)).findAll();
    }


    // ✅ Test for getUserProfile() - Success
    @Test
    void testGetUserProfile_ShouldReturnUserDto_WhenUserExists() {
        Role role = new Role();
        role.setName("RIDER");
        user.setRole(role);

        when(userRepository.findByMobileNumber("9885791402")).thenReturn(Optional.of(user));

        when(modelMapper.map(any(User.class), eq(UserDto.class))).thenAnswer(invocation -> {
            User source = invocation.getArgument(0);
            UserDto dto = new UserDto();
            dto.setFirstName(source.getFirstName());
            dto.setLastName(source.getLastName());
            dto.setGender(source.getGender());
            dto.setMobileNumber(source.getMobileNumber());
            dto.setRole(source.getRole() != null ? source.getRole().getName() : null);
            return dto;
        });

        UserDto result = userService.getUserProfile("9885791402");

        assertNotNull(result);
        assertEquals("RIDER", result.getRole());
        assertEquals("Gopal", result.getFirstName());
        verify(userRepository, times(1)).findByMobileNumber("9885791402");
    }

    // ✅ Test for getUserProfile() - Not Found
    @Test
    void testGetUserProfile_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByMobileNumber("9999999999")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserProfile("9999999999"));

        verify(userRepository, times(1)).findByMobileNumber("9999999999");
    }
}
