package com.cinemabooking.platform.service;

import com.cinemabooking.platform.model.request.CreateScreeningRequestDTO;
import com.cinemabooking.platform.model.response.HallOptionResponseDTO;
import com.cinemabooking.platform.model.response.MovieOptionResponseDTO;
import com.cinemabooking.platform.model.response.ScreeningResponseDTO;
import com.cinemabooking.platform.model.response.ScreeningSeatResponseDTO;

import java.util.List;


public interface ScreeningSerivce {
    ScreeningResponseDTO createScreening(CreateScreeningRequestDTO request);
    List<ScreeningSeatResponseDTO> getScreeningSeats(
            Long screeningId
    );
    List<ScreeningResponseDTO> getUpcomingScreenings();

    List<MovieOptionResponseDTO> getActiveMovieOptions();

    List<HallOptionResponseDTO> getActiveHallOptions();
}
