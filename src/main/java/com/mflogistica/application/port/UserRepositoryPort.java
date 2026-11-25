package com.mflogistica.application.port;

import com.mflogistica.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByUsername(String username);

    User save(User user);
}
