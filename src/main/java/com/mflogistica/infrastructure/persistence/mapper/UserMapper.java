package com.mflogistica.infrastructure.persistence.mapper;

import com.mflogistica.domain.model.User;
import com.mflogistica.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getFullName(),
                entity.getRole()
        );
    }

    public UserEntity toEntity(User domain) {
        UserEntity entity = new UserEntity();
        entity.setId(domain.id());
        entity.setEmail(domain.email());
        entity.setPassword(domain.password());
        entity.setFullName(domain.fullName());
        entity.setRole(domain.role());
        return entity;
    }
}

