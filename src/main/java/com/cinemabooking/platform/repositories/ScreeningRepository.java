package com.cinemabooking.platform.repositories;

import com.cinemabooking.platform.model.Screening;
import com.cinemabooking.platform.model.enums.ScreeningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {

    @Query(" SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Screening s "+
            "WHERE s.startTime< :hallAvailableAt AND s.hallAvailableAt > :startTime " +
            "AND s.hall.id = :hallId AND s.status != :excludedStatus")
    boolean existsConflictingScreening(@Param("hallAvailableAt") LocalDateTime hallAvailableAt,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("hallId") Long hallId,
                                       @Param("excludedStatus") ScreeningStatus excludedStatus);

}
