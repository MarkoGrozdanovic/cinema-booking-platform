package com.cinemabooking.platform.repositories;

import com.cinemabooking.platform.model.Booking;
import com.cinemabooking.platform.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.status = :status " +
            "AND b.expiresAt <= :now")
    List<Booking> findExpirePendingBookings(
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now);
}
