package com.cinemabooking.platform.repositories;

import com.cinemabooking.platform.model.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    boolean existsByNameIgnoreCaseAndCityIgnoreCase(
            String name,
            String city
    );
}
