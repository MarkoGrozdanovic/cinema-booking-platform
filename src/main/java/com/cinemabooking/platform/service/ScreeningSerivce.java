package com.cinemabooking.platform.service;

import com.cinemabooking.platform.model.request.CreateScreeningRequestDTO;
import com.cinemabooking.platform.model.response.ScreeningResponseDTO;


public interface ScreeningSerivce {
    ScreeningResponseDTO createScreening(CreateScreeningRequestDTO request);
}
