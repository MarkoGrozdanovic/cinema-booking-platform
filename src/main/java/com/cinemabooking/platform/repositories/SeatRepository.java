package com.cinemabooking.platform.repositories;

import com.cinemabooking.platform.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findAllByHallIdAndActiveTrue(Long hallId);

}
