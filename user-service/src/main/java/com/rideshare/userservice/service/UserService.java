package com.rideshare.userservice.service;

import com.rideshare.userservice.dto.ApiResponse;
import com.rideshare.userservice.dto.RegistrationRequest;
import com.rideshare.userservice.dto.UserDto;
import com.rideshare.userservice.entity.Role;
import com.rideshare.userservice.entity.User;
import com.rideshare.userservice.enums.RoleType;
import com.rideshare.userservice.repository.RoleRepository;
import com.rideshare.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public ApiResponse registerUser(RegistrationRequest request) {
        log.info("Registration attempt for mobileNumber={} with role={}", request.mobileNumber(), request.role());

        String requestedRole = request.role().toUpperCase();

        return userRepository.findByMobileNumber(request.mobileNumber())
                .map(existingUser -> {
                    String currentRole = existingUser.getRole().getName();
                    log.info("Existing user found: mobileNumber={}, role={}", existingUser.getMobileNumber(), currentRole);

                    if (RoleType.BOTH.name().equals(currentRole)) {
                        log.warn("User {} already has BOTH roles", existingUser.getMobileNumber());
                        return new ApiResponse(false, "You already have BOTH roles. Please login.");
                    }

                    if (currentRole.equals(requestedRole)) {
                        log.warn("User {} already registered with role {}", existingUser.getMobileNumber(), requestedRole);
                        return new ApiResponse(false, "You are already registered with this role. Please login.");
                    }

                    // Upgrade to BOTH
                    Role bothRole = roleRepository.findByName("BOTH")
                            .orElseThrow(() -> new RuntimeException("Role BOTH not found"));
                    existingUser.setRole(bothRole);
                    userRepository.save(existingUser);

                    log.info("User {} upgraded to BOTH roles", existingUser.getMobileNumber());
                    return new ApiResponse(true, "You are now registered with BOTH roles.");
                })
                .orElseGet(() -> {
                    // ✅ Manual mapping instead of ModelMapper
                    Role role = roleRepository.findByName(requestedRole)
                            .orElseThrow(() -> new RuntimeException("Role not found"));

                    User newUser = new User();
                    newUser.setFirstName(request.firstName());
                    newUser.setLastName(request.lastName());
                    newUser.setGender(request.gender());
                    newUser.setMobileNumber(request.mobileNumber());
                    newUser.setRole(role);

                    userRepository.save(newUser);

                    log.info("New user {} registered successfully with role={}", newUser.getMobileNumber(), requestedRole);
                    return new ApiResponse(true, "Registration successful as " + requestedRole);
                });
    }



    // ✅ Fetch all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserDto getUserProfile(String mobileNumber) {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return modelMapper.map(user, UserDto.class);
    }

}
