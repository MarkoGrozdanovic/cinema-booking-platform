package com.cinemabooking.platform.repositories;

import com.cinemabooking.platform.model.ScreeningSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ScreeningSeatRepository extends JpaRepository<ScreeningSeat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ss
            FROM ScreeningSeat ss
            WHERE ss.id IN :seatIds
            ORDER BY ss.id ASC
            """)
    List<ScreeningSeat> findAllByIdsWithLock(
            @Param("seatIds") Collection<Long> seatIds
    );
}
