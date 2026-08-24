package com.cinemabooking.platform.scheduler;

import com.cinemabooking.platform.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingExpirationSchedulerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingExpirationScheduler scheduler;

    @Test
    void expireBookings_shouldProcessEveryExpiredBooking() {
        when(bookingService.findExpiredPendingBookingIds())
                .thenReturn(List.of(3L, 5L, 8L));

        scheduler.expireBookings();

        var order = inOrder(bookingService);
        order.verify(bookingService).findExpiredPendingBookingIds();
        order.verify(bookingService).expirePendingBooking(3L);
        order.verify(bookingService).expirePendingBooking(5L);
        order.verify(bookingService).expirePendingBooking(8L);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void expireBookings_shouldContinueWhenOneBookingFails() {
        when(bookingService.findExpiredPendingBookingIds())
                .thenReturn(List.of(3L, 5L, 8L));
        doThrow(new RuntimeException("Stripe unavailable"))
                .when(bookingService).expirePendingBooking(5L);

        scheduler.expireBookings();

        verify(bookingService).expirePendingBooking(3L);
        verify(bookingService).expirePendingBooking(5L);
        verify(bookingService).expirePendingBooking(8L);
    }

    @Test
    void expireBookings_shouldDoNothingWhenNoBookingsExpired() {
        when(bookingService.findExpiredPendingBookingIds())
                .thenReturn(List.of());

        scheduler.expireBookings();

        verify(bookingService).findExpiredPendingBookingIds();
        verifyNoMoreInteractions(bookingService);
    }
}
