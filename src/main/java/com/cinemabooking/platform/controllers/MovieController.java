package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.request.CreateMovieRequestDTO;
import com.cinemabooking.platform.model.response.MovieResponseDTO;
import com.cinemabooking.platform.service.MovieService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cinemabooking.platform.model.request.UpdateMovieStatusRequestDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;

@Tag(
        name = "Movies",
        description = "Admin movie management"
)
@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponseDTO> createMovie(
            @Valid @RequestBody CreateMovieRequestDTO request
    ) {
        MovieResponseDTO response =
                movieService.createMovie(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<MovieResponseDTO>>
    getAllMovies() {
        return ResponseEntity.ok(
                movieService.getAllMovies()
        );
    }

    @PutMapping("/{movieId}/status")
    public ResponseEntity<MovieResponseDTO> updateMovieStatus(
            @PathVariable Long movieId,
            @Valid
            @RequestBody UpdateMovieStatusRequestDTO request
    ) {
        MovieResponseDTO response =
                movieService.updateMovieStatus(
                        movieId,
                        request.getActive()
                );

        return ResponseEntity.ok(response);
    }
}