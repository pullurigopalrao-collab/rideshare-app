package com.rideshare.userservice.service;

import com.rideshare.userservice.dto.ApiResponse;
import com.rideshare.userservice.dto.RegistrationRequest;
import com.rideshare.userservice.dto.UserDto;
import com.rideshare.userservice.entity.Role;
import com.rideshare.userservice.entity.User;
import com.rideshare.userservice.entity.UserRole;
import com.rideshare.userservice.exception.UserNotFoundException;
import com.rideshare.userservice.repository.RoleRepository;
import com.rideshare.userservice.repository.UserRepository;
import com.rideshare.userservice.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

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
    private UserRoleRepository userRoleRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private Role riderRole;
    private Role ownerRole;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        riderRole = new Role(1L, "RIDER", "Rider");
        ownerRole = new Role(3L, "OWNER", "Owner");

        user = new User();
        user.setFirstName("Gopal");
        user.setLastName("Rao");
        user.setMobileNumber("9885791402");

        userDto = new UserDto();
        userDto.setFirstName("Gopal");
        userDto.setLastName("Rao");
        userDto.setMobileNumber("9885791402");
    }

    // 1️⃣ New user registration
    @Test
    void testRegisterUser_NewUser_Rider() {
        RegistrationRequest request =
                new RegistrationRequest("John", "Doe", "M", "9999999999", "RIDER");

        when(userRepository.findByMobileNumber("9999999999"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByName("RIDER"))
                .thenReturn(Optional.of(riderRole));
        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));

        ApiResponse response = userService.registerUser(request);

        assertTrue(response.isSuccess());
        verify(userRepository).save(any(User.class));
        verify(userRoleRepository).save(any(UserRole.class));
    }

    // 2️⃣ Existing user + SAME role → reject
    @Test
    void testRegisterUser_ExistingUser_SameRole() {
        RegistrationRequest request =
                new RegistrationRequest("John", "Doe", "M", "9885791402", "RIDER");

        when(userRepository.findByMobileNumber("9885791402"))
                .thenReturn(Optional.of(user));
        when(roleRepository.findByName("RIDER"))
                .thenReturn(Optional.of(riderRole));
        when(userRoleRepository.existsByUserIdAndRoleId(any(), any()))
                .thenReturn(true);

        ApiResponse response = userService.registerUser(request);

        assertFalse(response.isSuccess());
        verify(userRoleRepository, never()).save(any());
    }

    // 3️⃣ Existing user + DIFFERENT role → add role
    @Test
    void testRegisterUser_ExistingUser_NewRole() {
        RegistrationRequest request =
                new RegistrationRequest("John", "Doe", "M", "9885791402", "OWNER");

        when(userRepository.findByMobileNumber("9885791402"))
                .thenReturn(Optional.of(user));
        when(roleRepository.findByName("OWNER"))
                .thenReturn(Optional.of(ownerRole));
        when(userRoleRepository.existsByUserIdAndRoleId(any(), any()))
                .thenReturn(false);

        ApiResponse response = userService.registerUser(request);

        assertTrue(response.isSuccess());
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void testRegisterUser_InvalidRole_ThrowsInvalidRoleException() {
        // Arrange
        RegistrationRequest request =
                new RegistrationRequest("John", "Doe", "M", "9999999999", "ADMIN");

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.registerUser(request));

        assertEquals("Invalid role: ADMIN", ex.getMessage());
        verify(roleRepository, times(1)).findByName("ADMIN");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(userRoleRepository);
    }

    // 5️⃣ getAllUsers
    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Stub the per-element mapping used by the service
        when(modelMapper.map(any(User.class), eq(UserDto.class)))
                .thenReturn(userDto);

        List<UserDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        verify(userRepository).findAll();
        verify(modelMapper, times(1)).map(any(User.class), eq(UserDto.class));
    }

    // 6️⃣ getUserProfile success
    @Test
    void testGetUserProfile_Success() {
        when(userRepository.findByMobileNumber("9885791402"))
                .thenReturn(Optional.of(user));

        when(modelMapper.map(any(User.class), eq(UserDto.class)))
                .thenReturn(userDto);

        UserDto result = userService.getUserProfile("9885791402");

        assertNotNull(result);
        verify(userRepository).findByMobileNumber("9885791402");
    }

    // 7️⃣ getUserProfile not found
    @Test
    void testGetUserProfile_NotFound() {
        when(userRepository.findByMobileNumber("9999999999"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserProfile("9999999999"));
    }
}
