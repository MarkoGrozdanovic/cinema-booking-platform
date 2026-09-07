package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.enums.PaymentStatus;
import com.cinemabooking.platform.model.response.AdminBookingResponseDTO;
import com.cinemabooking.platform.model.response.PageResponseDTO;
import com.cinemabooking.platform.service.BookingService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<AdminBookingResponseDTO>>
    getAllBookings(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page cannot be negative")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size cannot exceed 100")
            int size,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            BookingStatus bookingStatus,

            @RequestParam(required = false)
            PaymentStatus paymentStatus,

            @RequestParam(defaultValue = "false")
            boolean paymentNotStarted
    ) {
        return ResponseEntity.ok(
                bookingService.getAllBookingsForAdmin(
                        page,
                        size,
                        search,
                        bookingStatus,
                        paymentStatus,
                        paymentNotStarted
                )
        );
    }
}