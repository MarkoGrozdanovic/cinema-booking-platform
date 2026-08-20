package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.exceptions.BusinessException;
import com.cinemabooking.platform.exceptions.InvalidCredentialsException;
import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.request.LoginRequestDTO;
import com.cinemabooking.platform.model.request.RegisterRequestDTO;
import com.cinemabooking.platform.model.response.AuthResponseDTO;
import com.cinemabooking.platform.model.response.RegisteredUserResponseDTO;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import com.cinemabooking.platform.service.AuthService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    @Override
    @Transactional
    public RegisteredUserResponseDTO register(RegisterRequestDTO request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if(userRepository.existsByEmailIgnoreCase(normalizedEmail)){
            throw new BusinessException("A user with this email aready exist");
        }

        AppUser user = new AppUser();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(AppRole.CUSTOMER);
        user.setActive(true);

        AppUser savedUser = userRepository.save(user);

        return RegisteredUserResponseDTO.builder()
                .id(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        AppUser user = userRepository.
                findByEmailIgnoreCaseAndActiveTrue(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid email or password"
                ));

        if(!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )){
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(user);

        return AuthResponseDTO.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }
}
