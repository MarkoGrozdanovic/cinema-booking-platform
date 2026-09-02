package com.cinemabooking.platform.config;

import com.cinemabooking.platform.security.JwtAuthenticationFilter;
import com.cinemabooking.platform.security.RestSecurityExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestSecurityExceptionHandler securityExceptionHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, RestSecurityExceptionHandler securityExceptionHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityExceptionHandler = securityExceptionHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource) {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(securityExceptionHandler)
                        .accessDeniedHandler(securityExceptionHandler))
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/api/auth/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**")
                                .permitAll()

                                .requestMatchers("/api/admin/**")
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/screenings/upcoming"
                                ).permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/screenings/options/**"
                                ).hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/screenings"
                                ).hasRole("ADMIN")

                                .requestMatchers(
                                        "/api/screenings/**"
                                ).authenticated()

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/bookings"
                                )
                                .hasRole("CUSTOMER")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/bookings",
                                        "/api/bookings/**"
                                )
                                .hasRole("CUSTOMER")

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/payments/webhook"
                                ).permitAll()

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/payments/payment-intents"
                                ).hasRole("CUSTOMER")

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/bookings/**"
                                )
                                .hasRole("CUSTOMER")

                                .anyRequest()
                                .authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration
        );

        return source;
    }
}
