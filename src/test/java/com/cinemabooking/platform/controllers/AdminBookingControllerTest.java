package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.enums.PaymentStatus;
import com.cinemabooking.platform.model.response.AdminBookingResponseDTO;
import com.cinemabooking.platform.model.response.PageResponseDTO;
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

        PageResponseDTO<AdminBookingResponseDTO> pageResponse =
                PageResponseDTO.<AdminBookingResponseDTO>builder()
                        .content(List.of(booking))
                        .page(0)
                        .size(20)
                        .totalElements(1)
                        .totalPages(1)
                        .first(true)
                        .last(true)
                        .build();

        when(bookingService.getAllBookingsForAdmin(
                0,
                20,
                "marko",
                BookingStatus.CONFIRMED,
                PaymentStatus.SUCCEEDED,
                false
        )).thenReturn(pageResponse);

        ResponseEntity<PageResponseDTO<AdminBookingResponseDTO>> response =
                controller.getAllBookings(
                        0,
                        20,
                        "marko",
                        BookingStatus.CONFIRMED,
                        PaymentStatus.SUCCEEDED,
                        false
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(
                "BOOKING-001",
                response.getBody()
                        .getContent()
                        .get(0)
                        .getBookingReference()
        );

        verify(bookingService).getAllBookingsForAdmin(
                0,
                20,
                "marko",
                BookingStatus.CONFIRMED,
                PaymentStatus.SUCCEEDED,
                false
        );
    }
}
