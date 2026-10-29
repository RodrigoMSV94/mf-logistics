package com.mflogistica.infrastructure.persistence.repository;

import com.mflogistica.application.port.UserRepositoryPort;
import com.mflogistica.domain.model.User;
import com.mflogistica.infrastructure.persistence.entity.UserEntity;
import com.mflogistica.infrastructure.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(SpringDataUserRepository springDataUserRepository, UserMapper userMapper) {
        this.springDataUserRepository = springDataUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity saved = springDataUserRepository.save(userMapper.toEntity(user));
        return userMapper.toDomain(saved);
    }
}

