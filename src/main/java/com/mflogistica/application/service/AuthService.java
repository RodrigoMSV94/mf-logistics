package com.mflogistica.application.service;

import com.mflogistica.application.port.UserRepositoryPort;
import com.mflogistica.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final int MIN_PASSWORD_LENGTH = 16;

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void ensureAdminUser(String username, String rawPassword, String fullName) {
        if (userRepositoryPort.findByUsername(username).isPresent()) {
            return;
        }

        User admin = new User(
                null,
                username,
                passwordEncoder.encode(rawPassword),
                fullName,
                "ROLE_ADMIN"
        );

        userRepositoryPort.save(admin);
    }

    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        if (username == null || currentPassword == null || newPassword == null
                || newPassword.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }

        User user = userRepositoryPort.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(currentPassword, user.password())) {
            return false;
        }

        User updatedUser = new User(
                user.id(),
                user.username(),
                passwordEncoder.encode(newPassword),
                user.fullName(),
                user.role()
        );
        userRepositoryPort.save(updatedUser);
        LOGGER.info("Password changed for account '{}'", user.username());
        return true;
    }
}
