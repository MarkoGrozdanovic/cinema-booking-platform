package com.cinemabooking.platform.repositories;

import com.cinemabooking.platform.model.Hall;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
