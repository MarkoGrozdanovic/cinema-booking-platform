package com.cinemabooking.platform.controllers;


import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.request.CreateBookingRequestDTO;
import com.cinemabooking.platform.model.response.BookingResponseDTO;
import com.cinemabooking.platform.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.cinemabooking.platform.config.SecurityConfig;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import com.cinemabooking.platform.security.RestSecurityExceptionHandler;
import org.springframework.context.annotation.Import;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(BookingController.class)
@Import({
        SecurityConfig.class,
        RestSecurityExceptionHandler.class
})
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldCreateBooking() throws Exception{
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(1L)
                .bookingReference("BOOK-123")
                .movieTitle("The Dark Knight")
                .cinemaName("Central Cinema")
                .hallName("Hall 1")
                .status(BookingStatus.PENDING_PAYMENT)
                .screeningStartTime(
                        LocalDateTime.of(2026, 8, 20, 20, 0)
                )
                .expiresAt(
                        LocalDateTime.of(2026, 8, 19, 17, 15)
                )
                .totalPrice(new BigDecimal("1500.00"))
                .selectedSeats(List.of())
                .build();

        when(bookingService.createBooking(
                any(CreateBookingRequestDTO.class),
                eq(1L)))
                .thenReturn(response);

        String requestBody = """
                {
                    "screeningId": 10,
                    "screeningSeatIds": [100, 101]
                }
                """;

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(customerAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookingReference").value("BOOK-123"))
                .andExpect(jsonPath("$.movieTitle").value("The Dark Knight"))
                .andExpect(jsonPath("$.cinemaName").value("Central Cinema"))
                .andExpect(jsonPath("$.hallName").value("Hall 1"))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalPrice").value(1500.00));
    }

    @Test
    void shouldReturnBadRequestWhenScreeningIdIsMissing() throws Exception {
        String requestBody = """
            {
                "screeningSeatIds": [100, 101]
            }
            """;

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(customerAuthentication()))
                        .with(authentication(customerAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        Long authenticatedUserId = 1L;

        verifyNoInteractions(bookingService);
    }

    @Test
    void shouldReturnForbiddenWhenAdminCreatesBooking()
            throws Exception {

        String requestBody = """
            {
                "screeningSeatIds": [100, 101]
            }
            """;

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(customerAuthentication()))
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    private UsernamePasswordAuthenticationToken customerAuthentication() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("marko@example.com");
        user.setRole(AppRole.CUSTOMER);
        user.setActive(true);

        return UsernamePasswordAuthenticationToken.authenticated(
                user,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_CUSTOMER")
                )
        );
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        AppUser admin = new AppUser();
        admin.setId(2L);
        admin.setEmail("admin@cinema.com");
        admin.setRole(AppRole.ADMIN);
        admin.setActive(true);

        return UsernamePasswordAuthenticationToken.authenticated(
                admin,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );
    }
}
