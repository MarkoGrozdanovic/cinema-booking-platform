package com.cinemabooking.platform.service;

import com.cinemabooking.platform.model.request.CreateMovieRequestDTO;
import com.cinemabooking.platform.model.response.MovieResponseDTO;

import java.util.List;

public interface MovieService {

    MovieResponseDTO createMovie(
            CreateMovieRequestDTO request
    );

    List<MovieResponseDTO> getAllMovies();

    MovieResponseDTO updateMovieStatus(
            Long movieId,
            boolean active
    );
}