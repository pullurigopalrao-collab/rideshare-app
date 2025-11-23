package com.rideshare.authservice.dto;

public record JwtResponse(
        String token,
        String message)
{}
