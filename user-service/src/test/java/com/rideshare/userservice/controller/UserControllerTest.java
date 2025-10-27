package com.rideshare.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideshare.userservice.dto.ApiResponse;
import com.rideshare.userservice.dto.RegistrationRequest;
import com.rideshare.userservice.dto.UserDto;
import com.rideshare.userservice.exception.InvalidMobileNumberException;
import com.rideshare.userservice.exception.UserNotFoundException;
import com.rideshare.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for tests
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;  // <-- Mocked service

    @Autowired
    private ObjectMapper objectMapper;

    private RegistrationRequest riderRequest;

    @InjectMocks
    private UserController userController;


    @BeforeEach
    void setup() {
        riderRequest = new RegistrationRequest("John", "Doe", "Male", "9999999999", "RIDER");
    }

    @Test
    void testRegisterUser_Rider_Success() throws Exception {
        when(userService.registerUser(riderRequest))
                .thenReturn(new ApiResponse(true, "Registration successful as RIDER"));

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(riderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful as RIDER"));
    }
    @Test
    void testRegisterUser_ShouldReturnBadRequest_WhenFailure() throws Exception {
        RegistrationRequest request = new RegistrationRequest("John", "Doe", "M", "9999999999", "RIDER");

        ApiResponse apiResponse = new ApiResponse(false, "Mobile number already registered");

        when(userService.registerUser(any(RegistrationRequest.class))).thenReturn(apiResponse);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mobile number already registered"));
    }

    @Test
    void testProfileMe_Dummy() throws Exception {
        // Mock the Authentication object
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("9999999999"); // logged-in username

        // Mock service call
        when(userService.getUserProfile("9999999999"))
                .thenReturn(new UserDto("John", "Doe", "Male", "9999999999", "BOTH"));

        // Perform the request using MockMvc with a custom SecurityContext
        mockMvc.perform(get("/api/users/profile/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("BOTH"));
    }

    @Test
    void testRegisterUser_InternalServerError() throws Exception {
        RegistrationRequest req = new RegistrationRequest("John", "Doe", "M", "6666666666", "RIDER");

        when(userService.registerUser(any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void testGetUsers_ShouldReturnListOfUsers() throws Exception {
        List<UserDto> mockUsers = List.of(
                new UserDto("John", "Doe", "MALE", "9999999999", "RIDER"),
                new UserDto("Jane", "Smith", "FEMALE", "8888888888", "OWNER")
        );

        when(userService.getAllUsers()).thenReturn(mockUsers);
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].role").value("RIDER"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].role").value("OWNER"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetUserProfile_Success() throws Exception {
        UserDto mockUser = new UserDto("Gopal", "Rao", "MALE", "9885791402", "RIDER");

        when(userService.getUserProfile("9885791402")).thenReturn(mockUser);

        mockMvc.perform(get("/api/users/admin/profile/9885791402"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Gopal"))
                .andExpect(jsonPath("$.lastName").value("Rao"))
                .andExpect(jsonPath("$.role").value("RIDER"));
    }

    @Test
    void testGetUserProfileForAdmin_Success() throws Exception {
        UserDto mockUser = new UserDto("Lakshmi", "Kumar", "FEMALE", "9876543210", "OWNER");

        when(userService.getUserProfile("9876543210")).thenReturn(mockUser);

        mockMvc.perform(get("/api/users/admin/profile/9876543210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Lakshmi"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void testGetUserProfile_InvalidMobile() throws Exception {
        mockMvc.perform(get("/api/users/admin/profile/12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Mobile Number"))
                .andExpect(jsonPath("$.message").value("Invalid mobile number format. Must be 10 digits."));
    }


    @Test
    void testGetUserProfile_MissingMobile() throws Exception {
        mockMvc.perform(get("/api/users/admin/profile"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please provide a valid 10-digit mobile number in the URL path."));
    }

    @Test
    void testGetUserProfile_UserNotFound() throws Exception {
        when(userService.getUserProfile("9999999999"))
                .thenThrow(new UserNotFoundException("9999999999"));

        mockMvc.perform(get("/api/users/admin/profile/9999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User Not Found"));
    }

    @Test
    void testGetUserProfileForAdmin_ShouldReturnBadRequest_WhenMobileNumberIsEmpty() throws Exception {
        mockMvc.perform(get("/api/users/admin/profile/ "))  // or proper mapping path
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Mobile Number"))
                .andExpect(jsonPath("$.message").value("Mobile number cannot be empty"));
    }

    @Test
    void testGetUserProfileForAdmin_ShouldThrowInvalidMobileNumber_WhenNull() {
        assertThrows(InvalidMobileNumberException.class,
                () -> userController.getUserProfileForAdmin(null));
    }

}
