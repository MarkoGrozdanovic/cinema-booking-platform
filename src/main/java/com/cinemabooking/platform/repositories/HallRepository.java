package com.cinemabooking.platform.repositories;

import com.cinemabooking.platform.model.Hall;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT h
       FROM Hall h
       WHERE h.id = :hallId
       """)
    Optional<Hall> findByIdWithLock(
            @Param("hallId") Long hallId
    );

    @Query("""
        SELECT h
        FROM Hall h
        JOIN FETCH h.cinema c
        WHERE h.active = true
          AND c.active = true
        ORDER BY c.name ASC, h.name ASC
        """)
    List<Hall> findAllActiveWithActiveCinema();

    boolean existsByNameIgnoreCaseAndCinemaId(
            String name,
            Long cinemaId
    );


    @Query("""
        SELECT DISTINCT h
        FROM Hall h
        JOIN FETCH h.cinema
        LEFT JOIN FETCH h.seats
        ORDER BY h.cinema.name ASC, h.name ASC
        """)
    List<Hall> findAllWithCinemaAndSeats();
}
