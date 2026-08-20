package com.cinemabooking.platform.service;

import com.cinemabooking.platform.model.request.CreateBookingRequestDTO;
import com.cinemabooking.platform.model.response.BookingResponseDTO;

public interface BookingService {

    BookingResponseDTO createBooking(CreateBookingRequestDTO request, Long id);
    void expirePendingBookings();

}
