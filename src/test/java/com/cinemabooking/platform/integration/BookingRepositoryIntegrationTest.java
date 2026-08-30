package com.cinemabooking.platform.integration;

import com.cinemabooking.platform.model.Booking;
import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.repositories.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookingRepositoryIntegrationTest
        extends PostgreSQLIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void insertRequiredData() {
        jdbcTemplate.update("""
                WITH inserted_user AS (
                    INSERT INTO app_users (
                        first_name,
                        last_name,
                        email,
                        password_hash,
                        role,
                        active,
                        created_at
                    )
                    VALUES (
                        'Integration',
                        'User',
                        'integration@example.com',
                        'hashed-password',
                        'CUSTOMER',
                        TRUE,
                        CURRENT_TIMESTAMP
                    )
                    RETURNING id
                ),
                inserted_cinema AS (
                    INSERT INTO cinemas (
                        name,
                        address,
                        city,
                        active
                    )
                    VALUES (
                        'Test Cinema',
                        'Test Address',
                        'Belgrade',
                        TRUE
                    )
                    RETURNING id
                ),
                inserted_hall AS (
                    INSERT INTO cinema_halls (
                        name,
                        hall_type,
                        cinema_id,
                        active
                    )
                    SELECT
                        'Hall 1',
                        'STANDARD',
                        id,
                        TRUE
                    FROM inserted_cinema
                    RETURNING id
                ),
                inserted_movie AS (
                    INSERT INTO movies (
                        title,
                        duration_minutes,
                        release_date,
                        age_rating,
                        genre,
                        language,
                        director,
                        active
                    )
                    VALUES (
                        'Test Movie',
                        120,
                        CURRENT_DATE,
                        'GENERAL',
                        'DRAMA',
                        'English',
                        'Test Director',
                        TRUE
                    )
                    RETURNING id
                ),
                inserted_screening AS (
                    INSERT INTO screenings (
                        movie_id,
                        hall_id,
                        start_time,
                        end_time,
                        hall_available_at,
                        base_price,
                        status
                    )
                    SELECT
                        m.id,
                        h.id,
                        CURRENT_TIMESTAMP
                            + INTERVAL '1 day',
                        CURRENT_TIMESTAMP
                            + INTERVAL '1 day 2 hours',
                        CURRENT_TIMESTAMP
                            + INTERVAL '1 day 2 hours 20 minutes',
                        10.00,
                        'SCHEDULED'
                    FROM inserted_movie m
                    CROSS JOIN inserted_hall h
                    RETURNING id
                )
                INSERT INTO bookings (
                    booking_reference,
                    user_id,
                    screening_id,
                    status,
                    created_at,
                    expires_at,
                    total_price,
                    version
                )
                SELECT
                    data.booking_reference,
                    u.id,
                    s.id,
                    data.status,
                    data.created_at,
                    data.expires_at,
                    10.00,
                    0
                FROM inserted_user u
                CROSS JOIN inserted_screening s
                CROSS JOIN (
                    VALUES
                    (
                        'BK-EXPIRED-PENDING',
                        'PENDING_PAYMENT',
                        CURRENT_TIMESTAMP
                            - INTERVAL '20 minutes',
                        CURRENT_TIMESTAMP
                            - INTERVAL '5 minutes'
                    ),
                    (
                        'BK-FUTURE-PENDING',
                        'PENDING_PAYMENT',
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                            + INTERVAL '15 minutes'
                    ),
                    (
                        'BK-EXPIRED-CONFIRMED',
                        'CONFIRMED',
                        CURRENT_TIMESTAMP
                            - INTERVAL '20 minutes',
                        CURRENT_TIMESTAMP
                            - INTERVAL '5 minutes'
                    )
                ) AS data(
                    booking_reference,
                    status,
                    created_at,
                    expires_at
                )
                """);
    }

    @Test
    void shouldFindOnlyExpiredPendingBookings() {
        Booking expiredPendingBooking =
                bookingRepository
                        .findByBookingReference(
                                "BK-EXPIRED-PENDING"
                        )
                        .orElseThrow();

        List<Long> expiredBookingIds =
                bookingRepository
                        .findExpiredPendingBookingIds(
                                BookingStatus.PENDING_PAYMENT,
                                LocalDateTime.now()
                        );

        assertEquals(1, expiredBookingIds.size());
        assertEquals(
                expiredPendingBooking.getId(),
                expiredBookingIds.get(0)
        );
    }
}