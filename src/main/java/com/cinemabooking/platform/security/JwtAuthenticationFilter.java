package com.cinemabooking.platform.security;


import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.repositories.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            if (SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                AppUser user = userRepository
                        .findByEmailIgnoreCaseAndActiveTrue(email)
                        .orElse(null);

                if (user == null) {
                    log.warn(
                            "JWT authentication failed: active user not found for email={}",
                            email
                    );
                } else if (!jwtService.isTokenValid(token, user)) {
                    log.warn(
                            "JWT authentication failed: token validation returned false for email={}",
                            email
                    );
                } else {
                    authenticateUser(user, request);

                    log.debug(
                            "JWT authentication succeeded: email={}, role={}",
                            email,
                            user.getRole()
                    );
                }
            }
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn(
                    "JWT authentication failed for path {}: {}",
                    request.getRequestURI(),
                    exception.getMessage()
            );
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(
            AppUser user,
            HttpServletRequest request
    ) {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                );

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        user,
                        null,
                        List.of(authority)
                );

        authentication.setDetails(request);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
