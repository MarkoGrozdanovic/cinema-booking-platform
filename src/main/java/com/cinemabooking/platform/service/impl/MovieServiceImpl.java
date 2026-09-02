package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.exceptions.ResourceNotFoundException;
import com.cinemabooking.platform.model.Movie;
import com.cinemabooking.platform.model.request.CreateMovieRequestDTO;
import com.cinemabooking.platform.model.response.MovieResponseDTO;
import com.cinemabooking.platform.repositories.MovieRepository;
import com.cinemabooking.platform.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public MovieResponseDTO createMovie(
            CreateMovieRequestDTO request
    ) {
        Movie movie = new Movie();

        movie.setTitle(request.getTitle().trim());
        movie.setDescription(
                normalizeOptional(request.getDescription())
        );
        movie.setDurationMinutes(
                request.getDurationMinutes()
        );
        movie.setReleaseDate(request.getReleaseDate());
        movie.setAgeRating(request.getAgeRating());
        movie.setGenre(request.getGenre());
        movie.setLanguage(request.getLanguage().trim());
        movie.setDirector(request.getDirector().trim());
        movie.setPosterUrl(
                normalizeOptional(request.getPosterUrl())
        );
        movie.setTrailerUrl(
                normalizeOptional(request.getTrailerUrl())
        );
        movie.setActive(true);

        Movie savedMovie = movieRepository.save(movie);

        return toMovieResponse(savedMovie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponseDTO> getAllMovies() {
        return movieRepository
                .findAll(
                        Sort.by(
                                Sort.Direction.ASC,
                                "title"
                        )
                )
                .stream()
                .map(this::toMovieResponse)
                .toList();
    }

    @Override
    @Transactional
    public MovieResponseDTO updateMovieStatus(
            Long movieId,
            boolean active
    ) {
        Movie movie = movieRepository
                .findById(movieId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie with ID " + movieId
                                        + " was not found"
                        )
                );

        movie.setActive(active);

        return toMovieResponse(movie);
    }

    private MovieResponseDTO toMovieResponse(Movie movie) {
        return MovieResponseDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(
                        movie.getDurationMinutes()
                )
                .releaseDate(movie.getReleaseDate())
                .ageRating(movie.getAgeRating())
                .genre(movie.getGenre())
                .language(movie.getLanguage())
                .director(movie.getDirector())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .active(movie.isActive())
                .build();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}