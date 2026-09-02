package com.cinemabooking.platform.model.request;

import com.cinemabooking.platform.model.enums.AgeRating;
import com.cinemabooking.platform.model.enums.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMovieRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters"
    )
    private String description;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than zero")
    private Integer durationMinutes;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @NotNull(message = "Age rating is required")
    private AgeRating ageRating;

    @NotNull(message = "Genre is required")
    private Genre genre;

    @NotBlank(message = "Language is required")
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    @NotBlank(message = "Director is required")
    @Size(max = 100, message = "Director must not exceed 100 characters")
    private String director;

    @Size(max = 500, message = "Poster URL must not exceed 500 characters")
    private String posterUrl;

    @Size(max = 500, message = "Trailer URL must not exceed 500 characters")
    private String trailerUrl;
}