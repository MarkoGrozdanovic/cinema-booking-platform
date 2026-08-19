package com.cinemabooking.platform.model;

import com.cinemabooking.platform.model.enums.ScreeningStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screenings")
@Getter
@Setter
@NoArgsConstructor
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Movie is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "movie_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_screening_movie")
    )
    private Movie movie;

    @NotNull(message = "Hall is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "hall_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_screening_hall")
    )
    private Hall hall;

    @NotNull(message = "Screening start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "Screening end time is required")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @NotNull
    @Column(name = "hall_available_at", nullable = false)
    private LocalDateTime hallAvailableAt;

    @NotNull(message = "Base price is required")
    @DecimalMin(
            value = "0.01",
            message = "Base price must be greater than zero"
    )
    @Column(
            name = "base_price",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal basePrice;

    @NotNull(message = "Screening status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScreeningStatus status = ScreeningStatus.SCHEDULED;

    @OneToMany(
            mappedBy = "screening",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ScreeningSeat> screeningSeats = new ArrayList<>();

    public void addScreeningSeat(ScreeningSeat screeningSeat) {
        screeningSeats.add(screeningSeat);
        screeningSeat.setScreening(this);
    }

    public void removeScreeningSeat(ScreeningSeat screeningSeat) {
        screeningSeats.remove(screeningSeat);
        screeningSeat.setScreening(null);
    }
}