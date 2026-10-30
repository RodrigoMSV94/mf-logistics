package com.mflogistica.infrastructure.config;

import com.mflogistica.application.port.UserRepositoryPort;
import com.mflogistica.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    @Test
    void shouldLoadAuthenticationDetailsByUsername() {
        UserRepositoryPort userRepository = mock(UserRepositoryPort.class);
        when(userRepository.findByUsername("mfadmin"))
                .thenReturn(Optional.of(new User(1L, "mfadmin", "{bcrypt}encoded", "Admin", "ROLE_ADMIN")));

        var userDetails = new SecurityConfig().userDetailsService(userRepository).loadUserByUsername("mfadmin");

        assertEquals("mfadmin", userDetails.getUsername());
        assertEquals("{bcrypt}encoded", userDetails.getPassword());
        verify(userRepository).findByUsername("mfadmin");
    }
}
