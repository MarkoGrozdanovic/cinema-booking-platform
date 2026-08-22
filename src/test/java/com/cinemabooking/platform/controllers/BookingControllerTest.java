package com.cinemabooking.platform.controllers;


import com.cinemabooking.platform.config.SecurityConfig;
import com.cinemabooking.platform.exceptions.ResourceNotFoundException;
import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.request.CreateBookingRequestDTO;
import com.cinemabooking.platform.model.response.BookingResponseDTO;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import com.cinemabooking.platform.security.RestSecurityExceptionHandler;
import com.cinemabooking.platform.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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

    @Test
    void shouldReturnBookingById() throws Exception {
        BookingResponseDTO response = BookingResponseDTO.builder()
                .id(5L)
                .bookingReference("BK-123")
                .movieTitle("Interstellar")
                .cinemaName("Central Cinema")
                .hallName("Hall 1")
                .status(BookingStatus.PENDING_PAYMENT)
                .screeningStartTime(
                        LocalDateTime.of(2026, 9, 1, 20, 0)
                )
                .expiresAt(
                        LocalDateTime.of(2026, 8, 22, 18, 15)
                )
                .totalPrice(new BigDecimal("800.00"))
                .selectedSeats(List.of())
                .build();

        when(bookingService.getBookingById(5L, 1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/bookings/5")
                        .with(authentication(
                                customerAuthentication()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference")
                        .value("BK-123"))
                .andExpect(jsonPath("$.movieTitle")
                        .value("Interstellar"))
                .andExpect(jsonPath("$.cinemaName")
                        .value("Central Cinema"))
                .andExpect(jsonPath("$.hallName")
                        .value("Hall 1"))
                .andExpect(jsonPath("$.status")
                        .value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalPrice")
                        .value(800.00));


    }

    @Test
    void shouldReturnNotFoundWhenBookingIsNotOwned() throws Exception {
        when(bookingService.getBookingById(999L, 1L))
                .thenThrow(new ResourceNotFoundException(
                        "Booking with ID 999 was not found"
                ));

        mockMvc.perform(get("/api/bookings/999")
                        .with(authentication(customerAuthentication())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Booking with ID 999 was not found"));
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

    @Test
    void shouldCancelBooking() throws Exception {
        mockMvc.perform(delete("/api/bookings/5")
                        .with(authentication(
                                customerAuthentication()
                        )))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(bookingService)
                .cancelBooking(5L, 1L);
    }

    @Test
    void shouldReturnNotFoundWhenCancellingBookingIsNotOwned() throws Exception {

        doThrow(new ResourceNotFoundException(
                "Booking with ID 999 was not found"
        )).when(bookingService)
                .cancelBooking(999L, 1L);

        mockMvc.perform(delete("/api/bookings/999")
                        .with(authentication(
                                customerAuthentication()
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Booking with ID 999 was not found"));

        verify(bookingService)
                .cancelBooking(999L, 1L);
    }

    @Test
    void shouldReturnForbiddenWhenAdminCancelsBooking()
            throws Exception {

        mockMvc.perform(delete("/api/bookings/5")
                        .with(authentication(
                                adminAuthentication()
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message")
                        .value(
                                "You do not have permission to access this resource"
                        ));

        verifyNoInteractions(bookingService);
    }

    @Test
    void shouldReturnCustomerBookings() throws Exception {
        BookingResponseDTO booking =
                BookingResponseDTO.builder()
                        .id(5L)
                        .bookingReference("BK-123")
                        .movieTitle("Interstellar")
                        .cinemaName("Central Cinema")
                        .hallName("Hall 1")
                        .status(BookingStatus.PENDING_PAYMENT)
                        .screeningStartTime(
                                LocalDateTime.of(
                                        2026, 9, 1, 20, 0
                                )
                        )
                        .expiresAt(
                                LocalDateTime.of(
                                        2026, 8, 22, 18, 15
                                )
                        )
                        .totalPrice(new BigDecimal("800.00"))
                        .selectedSeats(List.of())
                        .build();

        when(bookingService.getAllBookings(1L))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/api/bookings")
                        .with(authentication(
                                customerAuthentication()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].bookingReference")
                        .value("BK-123"))
                .andExpect(jsonPath("$[0].movieTitle")
                        .value("Interstellar"))
                .andExpect(jsonPath("$[0].cinemaName")
                        .value("Central Cinema"))
                .andExpect(jsonPath("$[0].hallName")
                        .value("Hall 1"))
                .andExpect(jsonPath("$[0].status")
                        .value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$[0].totalPrice")
                        .value(800.00));

        verify(bookingService).getAllBookings(1L);
    }

    @Test
    void shouldReturnUnauthorizedWhenGettingBookingsWithoutAuthentication()
            throws Exception {

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication is required"));

        verifyNoInteractions(bookingService);
    }
}
