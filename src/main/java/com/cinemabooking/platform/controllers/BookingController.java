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

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
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

    @GetMapping("/bookings")
    public List<BookingResponseDTO> getAlLBookings(@AuthenticationPrincipal AppUser authenticatedUser){
       return bookingService.getAllBookings(
                authenticatedUser.getId()
        ); }

    @GetMapping("/bookings/{bookingId}")
    public BookingResponseDTO getBookingById(
            @PathVariable(name = "bookingId") Long bookingId,
            @AuthenticationPrincipal AppUser authenticatedUser
    ){
        return bookingService.getBookingById(
                bookingId,
                authenticatedUser.getId()
        );
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable(name = "bookingId") Long bookingId,
            @AuthenticationPrincipal AppUser authenticatedUser
    ){
        bookingService.cancelBooking(
                bookingId,
                authenticatedUser.getId()
        );

        return ResponseEntity.noContent().build();}
}
