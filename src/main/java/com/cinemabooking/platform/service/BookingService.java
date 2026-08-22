package com.cinemabooking.platform.service;

import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.request.CreateBookingRequestDTO;
import com.cinemabooking.platform.model.response.BookingResponseDTO;

import java.util.List;

public interface BookingService {

    BookingResponseDTO createBooking(CreateBookingRequestDTO request, Long id);
    void expirePendingBookings();

    List<BookingResponseDTO> getAllBookings(Long id);

    BookingResponseDTO getBookingById(Long bookingId, Long id);

    void cancelBooking(Long bookingId, Long userid);
}
