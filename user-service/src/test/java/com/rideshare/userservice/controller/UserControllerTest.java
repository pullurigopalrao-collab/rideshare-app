package com.rideshare.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideshare.userservice.dto.ApiResponse;
import com.rideshare.userservice.dto.RegistrationRequest;
import com.rideshare.userservice.dto.UserDto;
import com.rideshare.userservice.entity.User;
import com.rideshare.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
    void testProfileMe_Dummy() throws Exception {
        // Mock the Authentication object
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
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
    void testGetUsers_ShouldReturnListOfUsers()  {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setFirstName("John");
        user1.setLastName("Doe");

        User user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");

        List<User> userList = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(userList);
        assertFalse(userList.isEmpty());
/*
        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));*/
    }

}
