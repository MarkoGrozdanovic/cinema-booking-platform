package com.cinemabooking.platform.controllers;


import com.cinemabooking.platform.model.request.CreateBookingRequestDTO;
import com.cinemabooking.platform.model.response.BookingResponseDTO;
import com.cinemabooking.platform.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cinemabooking.platform.model.AppUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/test")
    public String testing(){
        return "Hello World";
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Valid @RequestBody CreateBookingRequestDTO requestDTO,
            @AuthenticationPrincipal AppUser authenticatedUser
            ){
        BookingResponseDTO response = bookingService.createBooking(
                requestDTO,
                authenticatedUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
