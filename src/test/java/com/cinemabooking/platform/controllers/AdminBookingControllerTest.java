package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.enums.PaymentStatus;
import com.cinemabooking.platform.model.response.AdminBookingResponseDTO;
import com.cinemabooking.platform.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private AdminBookingController controller;

    @Test
    void getAllBookings_shouldReturnAdminBookingResponses() {
        AdminBookingResponseDTO booking =
                AdminBookingResponseDTO.builder()
                        .id(1L)
                        .bookingReference("BOOKING-001")
                        .customerName("Marko Grozdanovic")
                        .customerEmail("marko@example.com")
                        .movieTitle("Interstellar")
                        .cinemaName("CineStar")
                        .hallName("Hall 1")
                        .bookingStatus(BookingStatus.CONFIRMED)
                        .paymentStatus(PaymentStatus.SUCCEEDED)
                        .totalPrice(new BigDecimal("1400.00"))
                        .build();

        when(bookingService.getAllBookingsForAdmin())
                .thenReturn(List.of(booking));

        ResponseEntity<List<AdminBookingResponseDTO>> response =
                controller.getAllBookings();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(
                "BOOKING-001",
                response.getBody().get(0).getBookingReference()
        );

        verify(bookingService).getAllBookingsForAdmin();
    }
}