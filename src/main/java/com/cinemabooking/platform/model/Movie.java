package com.cinemabooking.platform.model;

import com.cinemabooking.platform.model.enums.AgeRating;
import com.cinemabooking.platform.model.enums.Genre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Movie title is required")
    @Size(max = 150, message = "Movie title cannot exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Movie duration is required")
    @Positive(message = "Movie duration must be greater than zero")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @NotNull(message = "Release date is required")
    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @NotNull(message = "Age rating is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating", nullable = false, length = 30)
    private AgeRating ageRating;

    @NotNull(message = "Genre is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Genre genre;

    @NotBlank(message = "Movie language is required")
    @Size(max = 50, message = "Language cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String language;

    @NotBlank(message = "Director is required")
    @Size(max = 100, message = "Director name cannot exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String director;

    @Size(max = 500, message = "Poster URL cannot exceed 500 characters")
    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Size(max = 500, message = "Trailer URL cannot exceed 500 characters")
    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(nullable = false)
    private boolean active = true;
}