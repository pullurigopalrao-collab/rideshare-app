package com.rideshare.authservice.controller;

import com.rideshare.authservice.dto.JwtResponse;
import com.rideshare.authservice.dto.OtpRequest;
import com.rideshare.authservice.dto.OtpVerifyRequest;
import com.rideshare.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes /api/auth endpoints for OTP login flow.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Request OTP (only for registered users) */
    @PostMapping("/otp")
    public ResponseEntity<String> requestOtp(@RequestBody @Valid OtpRequest req) {
        log.info("Received OTP request for mobile={}", req.mobileNumber());
        authService.requestOtp(req);
        return ResponseEntity.ok("OTP sent successfully to your registered mobile number.");
    }

    /** Verify OTP and return JWT if successful */
    @PostMapping("/verify")
    public ResponseEntity<JwtResponse> verifyOtp(@RequestBody @Valid OtpVerifyRequest req) {
        log.info("Verifying OTP for mobile={}", req.mobileNumber());
        JwtResponse response = authService.verifyOtp(req);
        if (response.token() == null) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
