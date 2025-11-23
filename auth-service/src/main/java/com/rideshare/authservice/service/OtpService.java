package com.rideshare.authservice.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles OTP generation, storage (Redis), and async delivery event publish (Kafka).
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final SecureRandom RNG = new SecureRandom();
    private static final String OTP_PREFIX = "otp:";
    private static final Duration OTP_TTL = Duration.ofMinutes(3);

    private final StringRedisTemplate redis;
    private final KafkaTemplate<String, String> kafka;

    // Virtual thread executor for I/O-bound async tasks
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Generate a secure 6-digit OTP, store in Redis (TTL) and publish an event to NotificationService (Kafka).
     */
    public void generateAndSendOtp(String mobile) {
        int otp = 100000 + RNG.nextInt(900000);
        String key = OTP_PREFIX + mobile;
        redis.opsForValue().set(key, String.valueOf(otp), OTP_TTL);

        // Build a minimal JSON payload for NotificationService (it handles delivery).
        String event = String.format("{\"type\":\"OTP\",\"mobile\":\"%s\",\"otp\":\"%d\"}", mobile, otp);

        // Publish asynchronously using virtual threads to avoid blocking caller threads
        executor.submit(() -> {
            try {
                kafka.send("notification-events", mobile, event);
                log.debug("OTP event published for mobile={}", mobile);
            } catch (Exception e) {
                log.error("Failed to publish OTP event for mobile={}", mobile, e);
            }
        });
    }

    /**
     * Verify OTP by reading Redis key. If valid, delete the key (one-time use).
     */
    public boolean verifyOtp(String mobile, String otpInput) {
        String key = OTP_PREFIX + mobile;
        String stored = redis.opsForValue().get(key);
        if (stored == null) return false;
        boolean ok = stored.equals(otpInput);
        if (ok) redis.delete(key);
        return ok;
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
