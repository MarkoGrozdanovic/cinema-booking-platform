package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.response.AdminBookingResponseDTO;
import com.cinemabooking.platform.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<AdminBookingResponseDTO>> getAllBookings() {
        return ResponseEntity.ok(
                bookingService.getAllBookingsForAdmin()
        );
    }
}