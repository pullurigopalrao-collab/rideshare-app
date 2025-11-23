package com.rideshare.authservice;

import com.rideshare.authservice.security.JwtUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main entry point for the Authentication microservice.
 * Handles OTP login and JWT issuance for RideShare.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtUtil.JwtProps.class)
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
