package com.cinemabooking.platform.scheduler;

import com.cinemabooking.platform.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedDelay = 60_000)
    public void expireBookings() {
        List<Long> bookingIds =
                bookingService.findExpiredPendingBookingIds();

        for (Long bookingId : bookingIds) {
            try {
                bookingService.expirePendingBooking(
                        bookingId
                );
            } catch (RuntimeException exception) {
                log.error(
                        "Failed to expire booking with ID {}",
                        bookingId,
                        exception
                );
            }
        }
    }
}