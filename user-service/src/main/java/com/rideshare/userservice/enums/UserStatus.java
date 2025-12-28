package com.rideshare.userservice.enums;

public enum UserStatus {

    REGISTERED,   // User created, not yet logged in
    ACTIVE,       // User logged in at least once
    SUSPENDED,    // Temporarily blocked
    BLOCKED,      // Permanently blocked
    DELETED       // Soft deleted
}
