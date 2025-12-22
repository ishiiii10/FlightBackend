package com.chubb.auth.config;

import com.chubb.auth.entity.User;
import com.chubb.auth.enums.Role;
import com.chubb.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

/**
 * Seeds a default ADMIN user if configured.
 * Admin credentials are read from configuration (env/properties),
 * not from any public registration flow.
 * 
 * If the admin email already exists, it will be updated to ADMIN role
 * and password will be reset to the configured password.
 */
@Component
public class AdminBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void createDefaultAdmin() {
        // Only create/update admin if both email and password are provided via config.
        if (adminEmail == null || adminEmail.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.warn("Admin email or password not configured. Skipping admin bootstrap.");
            return;
        }

        Optional<User> existingUser = userRepository.findByEmail(adminEmail);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update existing user to ADMIN role and reset password
            if (user.getRole() != Role.ADMIN) {
                log.info("Updating existing user {} to ADMIN role", adminEmail);
                user.setRole(Role.ADMIN);
            }
            // Always update password to match configured password
            user.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(user);
            log.info("Admin user {} has been configured/updated successfully", adminEmail);
        } else {
            // Create new admin user
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info("Admin user {} has been created successfully", adminEmail);
        }
    }
}

