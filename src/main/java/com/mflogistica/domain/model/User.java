package com.mflogistica.domain.model;

public record User(
        Long id,
        String username,
        String password,
        String fullName,
        String role
) {
}
