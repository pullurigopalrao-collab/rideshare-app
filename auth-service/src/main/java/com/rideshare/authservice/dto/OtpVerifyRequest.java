package com.rideshare.authservice.dto;

public record OtpVerifyRequest(
        String mobileNumber,
        String otpCode)
{}
