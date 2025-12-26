package com.chubb.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.chubb.auth.dto.LoginRequest;
import com.chubb.auth.dto.LoginResponse;
import com.chubb.auth.dto.SignupRequest;
import com.chubb.auth.dto.UserProfileResponse;
import com.chubb.auth.dto.ChangePasswordRequest;
import com.chubb.auth.entity.User;
import com.chubb.auth.enums.Role;
import com.chubb.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists");
        }
    
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);  // hardcoded, no self-admin
    
        userRepository.save(user);
    }
    public LoginResponse login(LoginRequest request) {
        // Check if user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getRole().name());
    }

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getEmail()) // Using email as name for now, can be extended later
                .build();
    }
    public void changePassword(String email, ChangePasswordRequest request) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    String passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$";

    // Validate old password format
    if (!request.getOldPassword().matches(passwordPattern)) {
        throw new RuntimeException("Old password does not meet security requirements");
    }

    // Check old password against stored password
    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
        throw new RuntimeException("Old password is incorrect");
    }

    // Validate new password format
    if (!request.getNewPassword().matches(passwordPattern)) {
        throw new RuntimeException("New password must contain at least 1 uppercase, 1 lowercase, 1 number, 1 special character, and be minimum 6 characters long.");
    }

    // Check new password not same as old
    if (request.getNewPassword().equals(request.getOldPassword())) {
        throw new RuntimeException("New password cannot be the same as the old password");
    }

    // Everything valid => update
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
}
}