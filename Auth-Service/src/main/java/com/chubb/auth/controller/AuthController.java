package com.chubb.auth.controller;

import com.chubb.auth.dto.LoginRequest;
import com.chubb.auth.dto.LoginResponse;
import com.chubb.auth.dto.SignupRequest;
import com.chubb.auth.dto.UserProfileResponse;
import com.chubb.auth.service.AuthService;
import com.chubb.auth.dto.ChangePasswordRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @RequestHeader("X-User-Email") String email
    ) {
        return ResponseEntity.ok(authService.getProfile(email));
    }
    @PostMapping("/change-password")
public ResponseEntity<Void> changePassword(
        @RequestHeader("X-User-Email") String email,
        @Valid @RequestBody ChangePasswordRequest request
) {
    authService.changePassword(email, request);
    return ResponseEntity.ok().build();
}
}