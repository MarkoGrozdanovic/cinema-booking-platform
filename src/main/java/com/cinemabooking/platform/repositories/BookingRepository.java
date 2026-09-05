package com.cinemabooking.platform.repositories;

import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.Booking;
import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.response.AdminBookingResponseDTO;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);

    @EntityGraph(attributePaths = {
            "bookingItems",
            "bookingItems.screeningSeat",
            "bookingItems.screeningSeat.seat",
            "screening",
            "screening.movie",
            "screening.hall",
            "screening.hall.cinema"
    })
    List<Booking> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {
            "bookingItems",
            "bookingItems.screeningSeat",
            "bookingItems.screeningSeat.seat",
            "screening",
            "screening.movie",
            "screening.hall",
            "screening.hall.cinema"
    })
    Optional<Booking> findByIdAndUserId(Long bookingId, Long userId);

    @Query("""
        SELECT b.id
        FROM Booking b
        WHERE b.status = :status
          AND b.expiresAt <= :now
        """)
    List<Long> findExpiredPendingBookingIds(
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.id = :bookingId
        """)
    Optional<Booking> findByIdWithLock(
            @Param("bookingId") Long bookingId
    );

    boolean existsByScreeningIdAndStatusIn(
            Long screeningId,
            Collection<BookingStatus> statuses
    );

    @Query("""
        SELECT new com.cinemabooking.platform.model.response.AdminBookingResponseDTO(
            b.id,
            b.bookingReference,
            b.user.id,
            CONCAT(CONCAT(b.user.firstName, ' '), b.user.lastName),
            b.user.email,
            b.screening.movie.title,
            b.screening.hall.cinema.name,
            b.screening.hall.name,
            b.screening.startTime,
            b.status,
            p.status,
            b.totalPrice,
            b.createdAt
        )
        FROM Booking b
        LEFT JOIN Payment p
            ON p.booking = b
        ORDER BY b.createdAt DESC
        """)
    List<AdminBookingResponseDTO> findAllAdminBookingResponses();
}
