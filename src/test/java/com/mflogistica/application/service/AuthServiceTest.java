package com.mflogistica.application.service;

import com.mflogistica.application.port.UserRepositoryPort;
import com.mflogistica.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService = new AuthService(userRepositoryPort, passwordEncoder);

    @Test
    void shouldSaveNewPasswordOnlyAfterVerifyingCurrentPassword() {
        String oldPassword = "Old-Strong-Password-21!";
        String newPassword = "New-Strong-Password-21!";
        when(userRepositoryPort.findByUsername("mfadmin"))
                .thenReturn(Optional.of(new User(7L, "mfadmin", passwordEncoder.encode(oldPassword),
                        "Administrator", "ROLE_ADMIN")));

        assertTrue(authService.changePassword("mfadmin", oldPassword, newPassword));

        verify(userRepositoryPort).save(argThat(user -> user.id().equals(7L)
                && user.username().equals("mfadmin")
                && user.fullName().equals("Administrator")
                && user.role().equals("ROLE_ADMIN")
                && passwordEncoder.matches(newPassword, user.password())));
    }

    @Test
    void shouldNotChangePasswordWhenCurrentPasswordIsWrong() {
        when(userRepositoryPort.findByUsername("mfadmin"))
                .thenReturn(Optional.of(new User(7L, "mfadmin", passwordEncoder.encode("Old-Password-21!"),
                        "Administrator", "ROLE_ADMIN")));

        assertFalse(authService.changePassword("mfadmin", "wrong-password", "New-Strong-Password-21!"));

        verify(userRepositoryPort, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectPasswordShorterThanSixteenCharacters() {
        assertFalse(authService.changePassword("mfadmin", "current", "too-short"));

        verify(userRepositoryPort, never()).findByUsername("mfadmin");
        verify(userRepositoryPort, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
