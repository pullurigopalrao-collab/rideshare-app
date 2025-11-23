package com.rideshare.authservice.repository;

import com.rideshare.authservice.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    Optional<UserAuth> findByMobileNumber(String mobileNumber);
}
