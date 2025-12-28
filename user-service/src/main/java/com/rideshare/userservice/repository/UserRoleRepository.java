package com.rideshare.userservice.repository;

import com.rideshare.userservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
