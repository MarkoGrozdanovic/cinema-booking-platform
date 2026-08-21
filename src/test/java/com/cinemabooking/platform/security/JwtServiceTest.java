package com.cinemabooking.platform.security;

import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String testSecret;

    @BeforeEach
    void setUp() {
        testSecret = Base64.getEncoder()
                .encodeToString(
                        "01234567890123456789012345678901"
                                .getBytes(StandardCharsets.UTF_8)
                );

        jwtService = new JwtService(
                testSecret,
                900_000
        );
    }

    @Test
    void shouldGenerateAndValidateToken() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("marko@example.com");
        user.setRole(AppRole.CUSTOMER);
        user.setActive(true);

        String token = jwtService.generateToken(user);

        assertEquals(
                "marko@example.com",
                jwtService.extractEmail(token)
        );

        assertTrue(
                jwtService.isTokenValid(token, user)
        );
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        AppUser tokenOwner = new AppUser();
        tokenOwner.setId(1L);
        tokenOwner.setEmail("marko@example.com");
        tokenOwner.setRole(AppRole.CUSTOMER);
        tokenOwner.setActive(true);

        AppUser differentUser = new AppUser();
        differentUser.setId(2L);
        differentUser.setEmail("another@example.com");
        differentUser.setRole(AppRole.CUSTOMER);
        differentUser.setActive(true);

        String token = jwtService.generateToken(tokenOwner);

        boolean valid = jwtService.isTokenValid(
                token,
                differentUser
        );

        assertFalse(valid);
    }

    @Test
    void shouldRejectTokenForInactiveUser() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("marko@example.com");
        user.setRole(AppRole.CUSTOMER);
        user.setActive(true);

        String token = jwtService.generateToken(user);

        user.setActive(false);

        boolean valid = jwtService.isTokenValid(token, user);

        assertFalse(valid);
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtService expiredJwtService = new JwtService(
                testSecret,
                -1_000
        );

        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("marko@example.com");
        user.setRole(AppRole.CUSTOMER);
        user.setActive(true);

        String expiredToken =
                expiredJwtService.generateToken(user);

        boolean valid = expiredJwtService.isTokenValid(
                expiredToken,
                user
        );

        assertFalse(valid);
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        String differentSecret = Base64.getEncoder()
                .encodeToString(
                        "abcdefghijklmnopqrstuvwxyz123456"
                                .getBytes(StandardCharsets.UTF_8)
                );

        JwtService differentJwtService = new JwtService(
                differentSecret,
                900_000
        );

        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("marko@example.com");
        user.setRole(AppRole.CUSTOMER);
        user.setActive(true);

        String token = jwtService.generateToken(user);

        boolean valid = differentJwtService.isTokenValid(
                token,
                user
        );

        assertFalse(valid);
    }
}