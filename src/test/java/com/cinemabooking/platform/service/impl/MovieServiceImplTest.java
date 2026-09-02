package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.exceptions.ResourceNotFoundException;
import com.cinemabooking.platform.model.Movie;
import com.cinemabooking.platform.model.enums.AgeRating;
import com.cinemabooking.platform.model.enums.Genre;
import com.cinemabooking.platform.model.request.CreateMovieRequestDTO;
import com.cinemabooking.platform.model.response.MovieResponseDTO;
import com.cinemabooking.platform.repositories.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    @Test
    void createMovie_shouldNormalizeSaveAndReturnMovie() {
        CreateMovieRequestDTO request =
                new CreateMovieRequestDTO(
                        "  Inception  ",
                        "  Dream-sharing thriller  ",
                        148,
                        LocalDate.of(2010, 7, 16),
                        AgeRating.TWELVE_PLUS,
                        Genre.SCIENCE_FICTION,
                        "  English  ",
                        "  Christopher Nolan  ",
                        "  https://example.com/poster.jpg  ",
                        "   "
                );

        when(movieRepository.save(any(Movie.class)))
                .thenAnswer(invocation -> {
                    Movie movie = invocation.getArgument(0);
                    movie.setId(10L);
                    return movie;
                });

        MovieResponseDTO response =
                movieService.createMovie(request);

        ArgumentCaptor<Movie> captor =
                ArgumentCaptor.forClass(Movie.class);

        verify(movieRepository).save(captor.capture());

        Movie savedMovie = captor.getValue();

        assertAll(
                () -> assertEquals(
                        "Inception",
                        savedMovie.getTitle()
                ),
                () -> assertEquals(
                        "Dream-sharing thriller",
                        savedMovie.getDescription()
                ),
                () -> assertEquals(
                        148,
                        savedMovie.getDurationMinutes()
                ),
                () -> assertEquals(
                        AgeRating.TWELVE_PLUS,
                        savedMovie.getAgeRating()
                ),
                () -> assertEquals(
                        Genre.SCIENCE_FICTION,
                        savedMovie.getGenre()
                ),
                () -> assertEquals(
                        "English",
                        savedMovie.getLanguage()
                ),
                () -> assertEquals(
                        "Christopher Nolan",
                        savedMovie.getDirector()
                ),
                () -> assertEquals(
                        "https://example.com/poster.jpg",
                        savedMovie.getPosterUrl()
                ),
                () -> assertNull(
                        savedMovie.getTrailerUrl()
                ),
                () -> assertTrue(savedMovie.isActive())
        );

        assertAll(
                () -> assertEquals(10L, response.getId()),
                () -> assertEquals(
                        "Inception",
                        response.getTitle()
                ),
                () -> assertEquals(
                        148,
                        response.getDurationMinutes()
                ),
                () -> assertTrue(response.isActive())
        );
    }

    @Test
    void createMovie_shouldConvertBlankOptionalValuesToNull() {
        CreateMovieRequestDTO request =
                new CreateMovieRequestDTO(
                        "Inception",
                        "   ",
                        148,
                        LocalDate.of(2010, 7, 16),
                        AgeRating.TWELVE_PLUS,
                        Genre.SCIENCE_FICTION,
                        "English",
                        "Christopher Nolan",
                        null,
                        ""
                );

        when(movieRepository.save(any(Movie.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        MovieResponseDTO response =
                movieService.createMovie(request);

        assertNull(response.getDescription());
        assertNull(response.getPosterUrl());
        assertNull(response.getTrailerUrl());
    }

    @Test
    void getAllMovies_shouldReturnMappedMovies() {
        Movie movie = new Movie();
        movie.setId(10L);
        movie.setTitle("Inception");
        movie.setDescription("Description");
        movie.setDurationMinutes(148);
        movie.setReleaseDate(
                LocalDate.of(2010, 7, 16)
        );
        movie.setAgeRating(AgeRating.TWELVE_PLUS);
        movie.setGenre(Genre.SCIENCE_FICTION);
        movie.setLanguage("English");
        movie.setDirector("Christopher Nolan");
        movie.setActive(true);

        when(movieRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(movie));

        List<MovieResponseDTO> response =
                movieService.getAllMovies();

        assertEquals(1, response.size());

        MovieResponseDTO result = response.get(0);

        assertAll(
                () -> assertEquals(10L, result.getId()),
                () -> assertEquals(
                        "Inception",
                        result.getTitle()
                ),
                () -> assertEquals(
                        LocalDate.of(2010, 7, 16),
                        result.getReleaseDate()
                ),
                () -> assertEquals(
                        Genre.SCIENCE_FICTION,
                        result.getGenre()
                ),
                () -> assertTrue(result.isActive())
        );

        verify(movieRepository).findAll(any(Sort.class));
    }

    @Test
    void updateMovieStatus_shouldUpdateAndReturnMovie() {
        Movie movie = new Movie();
        movie.setId(10L);
        movie.setTitle("Inception");
        movie.setDurationMinutes(148);
        movie.setReleaseDate(
                LocalDate.of(2010, 7, 16)
        );
        movie.setAgeRating(AgeRating.TWELVE_PLUS);
        movie.setGenre(Genre.SCIENCE_FICTION);
        movie.setLanguage("English");
        movie.setDirector("Christopher Nolan");
        movie.setActive(true);

        when(movieRepository.findById(10L))
                .thenReturn(Optional.of(movie));

        MovieResponseDTO response =
                movieService.updateMovieStatus(
                        10L,
                        false
                );

        assertFalse(movie.isActive());
        assertFalse(response.isActive());
        assertEquals(10L, response.getId());
        assertEquals("Inception", response.getTitle());

        verify(movieRepository).findById(10L);
    }

    @Test
    void updateMovieStatus_shouldThrowWhenMovieDoesNotExist() {
        when(movieRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> movieService.updateMovieStatus(
                        999L,
                        false
                )
        );

        assertEquals(
                "Movie with ID 999 was not found",
                exception.getMessage()
        );
    }
}