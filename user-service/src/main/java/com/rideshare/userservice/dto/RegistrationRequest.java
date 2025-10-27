package com.rideshare.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name can be at most 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name can be at most 50 characters")
        String lastName,

        @NotBlank(message = "Gender is required")
        String gender,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "\\d{10}", message = "Mobile number must be 10 digits")
        String mobileNumber,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "RIDER|OWNER|BOTH", message = "Role must be RIDER, OWNER, or BOTH")
        String role
) {}