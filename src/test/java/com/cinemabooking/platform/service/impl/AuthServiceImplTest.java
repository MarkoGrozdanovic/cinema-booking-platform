package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.request.LoginRequestDTO;
import com.cinemabooking.platform.model.request.RegisterRequestDTO;
import com.cinemabooking.platform.model.response.AuthResponseDTO;
import com.cinemabooking.platform.model.response.RegisteredUserResponseDTO;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.cinemabooking.platform.exceptions.BusinessException;
import com.cinemabooking.platform.exceptions.InvalidCredentialsException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;



    @Test
    void shouldRegisterCustomerSuccessfully() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setFirstName(" Marko ");
        request.setLastName(" Grozdanovic ");
        request.setEmail(" MARKO@EXAMPLE.COM ");
        request.setPassword("SecurePassword123");

        when(appUserRepository.existsByEmailIgnoreCase(
                "marko@example.com"
        )).thenReturn(false);

        when(passwordEncoder.encode("SecurePassword123"))
                .thenReturn("encoded-password");

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> {
                    AppUser savedUser = invocation.getArgument(0);
                    savedUser.setId(1L);
                    return savedUser;
                });

        RegisteredUserResponseDTO response =
                authService.register(request);

        assertAll(
                () -> assertEquals(1L, response.getId()),
                () -> assertEquals("Marko", response.getFirstName()),
                () -> assertEquals(
                        "Grozdanovic",
                        response.getLastName()
                ),
                () -> assertEquals(
                        "marko@example.com",
                        response.getEmail()
                ),
                () -> assertEquals(
                        AppRole.CUSTOMER,
                        response.getRole()
                )
        );

        verify(passwordEncoder)
                .encode("SecurePassword123");

        verify(appUserRepository)
                .save(any(AppUser.class));
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setFirstName("Marko");
        request.setLastName("Grozdanovic");
        request.setEmail(" MARKO@EXAMPLE.COM ");
        request.setPassword("SecurePassword123");

        when(appUserRepository.existsByEmailIgnoreCase(
                "marko@example.com"
        )).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "A user with this email already exists",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(any());

        verify(appUserRepository, never())
                .save(any(AppUser.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(" MARKO@EXAMPLE.COM ");
        request.setPassword("SecurePassword123");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setFirstName("Marko");
        user.setLastName("Grozdanovic");
        user.setEmail("marko@example.com");
        user.setPassword("encoded-password");
        user.setRole(AppRole.CUSTOMER);
        user.setActive(true);

        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrue(
                "marko@example.com"
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "SecurePassword123",
                "encoded-password"
        )).thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("generated-jwt-token");

        AuthResponseDTO response = authService.login(request);

        assertAll(
                () -> assertEquals(1L, response.getUserId()),
                () -> assertEquals("Marko", response.getFirstName()),
                () -> assertEquals(
                        "Grozdanovic",
                        response.getLastName()
                ),
                () -> assertEquals(
                        "marko@example.com",
                        response.getEmail()
                ),
                () -> assertEquals(
                        AppRole.CUSTOMER,
                        response.getRole()
                ),
                () -> assertEquals(
                        "generated-jwt-token",
                        response.getAccessToken()
                ),
                () -> assertEquals(
                        "Bearer",
                        response.getTokenType()
                )
        );

        verify(jwtService).generateToken(user);
    }

    @Test
    void shouldRejectLoginWhenActiveUserIsNotFound() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("unknown@example.com");
        request.setPassword("SecurePassword123");

        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrue(
                "unknown@example.com"
        )).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(any(AppUser.class));
    }

    @Test
    void shouldRejectLoginWhenPasswordIsIncorrect() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("marko@example.com");
        request.setPassword("WrongPassword123");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("marko@example.com");
        user.setPassword("encoded-password");
        user.setRole(AppRole.CUSTOMER);
        user.setActive(true);

        when(appUserRepository.findByEmailIgnoreCaseAndActiveTrue(
                "marko@example.com"
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword123",
                "encoded-password"
        )).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(jwtService, never())
                .generateToken(any(AppUser.class));
    }
}
