package com.rideshare.authservice.service;

import com.rideshare.authservice.dto.JwtResponse;
import com.rideshare.authservice.dto.OtpRequest;
import com.rideshare.authservice.dto.OtpVerifyRequest;
import com.rideshare.authservice.exception.UserNotRegisteredException;
import com.rideshare.authservice.entity.UserAuth;
import com.rideshare.authservice.repository.UserAuthRepository;
import com.rideshare.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Auth orchestration: request OTP (only for already registered users) and verify OTP to issue JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAuthRepository userRepo;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    /**
     * Request OTP for login. Reject if user is not registered (explicit registration required).
     */
    @Transactional(readOnly = true)
    public void requestOtp(OtpRequest req) {
        boolean exists = userRepo.findByMobileNumber(req.mobileNumber()).isPresent();
        if (!exists) {
            throw new UserNotRegisteredException(
                    "User not registered. Please register first using /api/users/register.");
        }
        otpService.generateAndSendOtp(req.mobileNumber());
    }

    /**
     * Verify OTP and return JWT response. Marks user verified on first success.
     */
    @Transactional
    public JwtResponse verifyOtp(OtpVerifyRequest req) {
        boolean ok = otpService.verifyOtp(req.mobileNumber(), req.otpCode());
        if (!ok) {
            return new JwtResponse(null, "Invalid or expired OTP");
        }

        UserAuth user = userRepo.findByMobileNumber(req.mobileNumber())
                .orElseThrow(() -> new UserNotRegisteredException(
                        "User not registered. Please register first using /api/users/register."));

        if (!user.isVerified()) {
            user.setVerified(true);
            userRepo.save(user); // update only the verification flag once after first successful OTP verification
        }

        // Embed role and verified flag as claims
        String token = jwtUtil.generateToken(
                user.getMobileNumber(),
                Map.of("role", user.getRole(), "verified", user.isVerified())
        );

        return new JwtResponse(token, "Login successful");
    }
}
