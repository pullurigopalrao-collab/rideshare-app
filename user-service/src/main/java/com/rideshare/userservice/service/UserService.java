package com.rideshare.userservice.service;

import com.rideshare.userservice.dto.ApiResponse;
import com.rideshare.userservice.dto.RegistrationRequest;
import com.rideshare.userservice.dto.UserDto;
import com.rideshare.userservice.entity.Role;
import com.rideshare.userservice.entity.User;
import com.rideshare.userservice.entity.UserRole;
import com.rideshare.userservice.enums.UserStatus;
import com.rideshare.userservice.exception.UserNotFoundException;
import com.rideshare.userservice.repository.RoleRepository;
import com.rideshare.userservice.repository.UserRepository;
import com.rideshare.userservice.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.modelMapper = modelMapper;
    }

    public ApiResponse registerUser(RegistrationRequest request) {

        log.info("Registration attempt for mobileNumber={} with role={}",
                request.mobileNumber(), request.role());

        String requestedRole = request.role().toUpperCase();

        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new RuntimeException("Invalid role: " + requestedRole));

        return userRepository.findByMobileNumber(request.mobileNumber())
                .map(existingUser -> {

                    boolean alreadyHasRole =
                            userRoleRepository.existsByUserIdAndRoleId(
                                    existingUser.getId(), role.getId()
                            );

                    if (alreadyHasRole) {
                        log.warn("User {} already registered with role {}",
                                existingUser.getMobileNumber(), requestedRole);

                        return new ApiResponse(
                                false,
                                "You are already registered with this role. Please login."
                        );
                    }

                    // ✅ Add NEW role to existing user
                    UserRole newUserRole = new UserRole();
                    newUserRole.setUser(existingUser);
                    newUserRole.setRole(role);

                    userRoleRepository.save(newUserRole);

                    log.info("Added new role {} to existing user {}",
                            requestedRole, existingUser.getMobileNumber());

                    return new ApiResponse(
                            true,
                            "You are now registered as " + requestedRole + ". Please login."
                    );
                })
                .orElseGet(() -> {

                    // 1️⃣ Create new user
                    User newUser = new User();
                    newUser.setFirstName(request.firstName());
                    newUser.setLastName(request.lastName());
                    newUser.setGender(request.gender());
                    newUser.setMobileNumber(request.mobileNumber());
                    newUser.setStatus(UserStatus.REGISTERED);

                    User savedUser = userRepository.save(newUser);

                    // 2️⃣ Assign initial role
                    UserRole userRole = new UserRole();
                    userRole.setUser(savedUser);
                    userRole.setRole(role);

                    userRoleRepository.save(userRole);

                    log.info("New user registered successfully. mobileNumber={}, role={}",
                            savedUser.getMobileNumber(), requestedRole);

                    return new ApiResponse(
                            true,
                            "Registration successful as " + requestedRole
                    );
                });
    }




    // ✅ Fetch all users
    public List<UserDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> {

                    UserDto dto = modelMapper.map(user, UserDto.class);
                    List<String> roles = userRoleRepository.findByUserId(user.getId())
                            .stream()
                            .map(ur -> ur.getRole().getName())
                            .toList();
                    dto.setRoles(roles);
                    return dto;
                })
                .toList();
    }



    // ✅ Fetch user profile by mobile number
    @Cacheable(value = "userProfiles", key = "#mobileNumber")
    public UserDto getUserProfile(String mobileNumber) {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new UserNotFoundException(mobileNumber));

        List<String> roles = userRoleRepository.findByUserId(user.getId())
                .stream()
                .map(ur -> ur.getRole().getName())
                .toList();

        UserDto dto = modelMapper.map(user, UserDto.class);
        dto.setRoles(roles);
        return dto;

    }

}
