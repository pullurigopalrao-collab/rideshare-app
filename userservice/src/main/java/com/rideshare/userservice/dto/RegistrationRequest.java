package com.rideshare.userservice.dto;

public record RegistrationRequest(
        String firstName,
        String lastName,
        String gender,
        String mobileNumber,
        String role // RIDER, OWNER, BOTH
) {}