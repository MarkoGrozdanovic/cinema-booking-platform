package com.cinemabooking.platform.model.response;

import com.cinemabooking.platform.model.enums.AgeRating;
import com.cinemabooking.platform.model.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private AgeRating ageRating;
    private Genre genre;
    private String language;
    private String director;
    private String posterUrl;
    private String trailerUrl;
    private boolean active;
}