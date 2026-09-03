package com.cinemabooking.platform.service;

import com.cinemabooking.platform.model.request.CreateHallRequestDTO;
import com.cinemabooking.platform.model.response.HallResponseDTO;

import java.util.List;

public interface HallService {

    HallResponseDTO createHall(
            CreateHallRequestDTO request
    );

    List<HallResponseDTO> getAllHalls();
}