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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

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
                any(CreateBookingRequestDTO.class)
        )).thenReturn(response);

        String requestBody = """
                {
                    "userId": 1,
                    "screeningId": 10,
                    "screeningSeatIds": [100, 101]
                }
                """;

        mockMvc.perform(post("/api/bookings")
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
    void shouldReturnBadRequestWhenUserIdIsMissing() throws Exception {
        String requestBody = """
            {
                "screeningId": 10,
                "screeningSeatIds": [100, 101]
            }
            """;

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(bookingService, never())
                .createBooking(any(CreateBookingRequestDTO.class));
    }
}
