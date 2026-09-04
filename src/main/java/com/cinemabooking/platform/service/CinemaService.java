package com.cinemabooking.platform.service;

import com.cinemabooking.platform.model.request.CreateCinemaRequestDTO;
import com.cinemabooking.platform.model.response.CinemaResponseDTO;

import java.util.List;

public interface CinemaService {

    CinemaResponseDTO createCinema(
            CreateCinemaRequestDTO request
    );

    List<CinemaResponseDTO> getAllCinemas();

    CinemaResponseDTO updateCinemaStatus(
            Long cinemaId,
            boolean active
    );
}