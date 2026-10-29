package com.mflogistica.application.port;

import com.mflogistica.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByEmail(String email);

    User save(User user);
}

