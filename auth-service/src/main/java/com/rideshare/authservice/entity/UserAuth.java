package com.rideshare.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an already-registered user allowed to request OTP login.
 */
@Entity
@Table(name = "auth_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique mobile number used for OTP login. */
    @Column(unique = true, nullable = false)
    private String mobileNumber;

    /** Role assigned: USER, OWNER, ADMIN, etc. */
    @Column(nullable = false)
    private String role;

    /** Flag to indicate first-time OTP verification completed. */
    @Column(nullable = false)
    private boolean verified;
}
