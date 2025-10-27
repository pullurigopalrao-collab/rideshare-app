package com.rideshare.userservice.controller;

import com.rideshare.userservice.dto.ApiResponse;
import com.rideshare.userservice.dto.RegistrationRequest;
import com.rideshare.userservice.dto.UserDto;
import com.rideshare.userservice.exception.InvalidMobileNumberException;
import com.rideshare.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    public static final String REGEX = "\\d{10}";
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegistrationRequest request) {
        ApiResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/profile/me")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {
        String loggedInUsername = authentication.getName();
        log.info("Fetching profile for user: {}", loggedInUsername);
        UserDto userProfile = userService.getUserProfile(loggedInUsername);
        return ResponseEntity.ok(userProfile);
    }

    // For Admin to view others' profiles
    @GetMapping("/admin/profile/{mobileNumber}")
    public UserDto getUserProfileForAdmin(@PathVariable String mobileNumber) {
        // Null or blank check
        if (mobileNumber == null || mobileNumber.isBlank()) {
            throw new InvalidMobileNumberException("Mobile number cannot be empty");
        }
        // 10-digit format check
        if (!mobileNumber.matches(REGEX)) {
            throw new InvalidMobileNumberException("Invalid mobile number format. Must be 10 digits.");
        }
        return userService.getUserProfile(mobileNumber);
    }

    // ✅ New fallback endpoint when /admin has no mobile number
    @GetMapping({"/admin/profile", "/admin/profile/"})
    public ResponseEntity<Map<String, Object>> handleMissingMobileNumber() {
        throw new InvalidMobileNumberException("Please provide a valid 10-digit mobile number in the URL path.");
    }

}
