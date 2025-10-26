package com.rideshare.userservice.dto;

public record UserDto (
     String firstName,
     String lastName,
     String gender,
     String mobileNumber,
     String role // e.g., RIDER / OWNER / BOTH
){}
