package com.mflogistica.domain.model;

public record User(
        Long id,
        String email,
        String password,
        String fullName,
        String role
) {
}

