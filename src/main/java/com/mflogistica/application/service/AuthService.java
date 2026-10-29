package com.mflogistica.application.service;

import com.mflogistica.application.port.UserRepositoryPort;
import com.mflogistica.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void ensureAdminUser(String email, String rawPassword, String fullName) {
        if (userRepositoryPort.findByEmail(email).isPresent()) {
            return;
        }

        User admin = new User(
                null,
                email,
                passwordEncoder.encode(rawPassword),
                fullName,
                "ROLE_ADMIN"
        );

        userRepositoryPort.save(admin);
    }
}

