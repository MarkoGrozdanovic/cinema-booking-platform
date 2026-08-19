package com.cinemabooking.platform.model;

import com.cinemabooking.platform.model.enums.ScreeningSeatStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "screening_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_screening_seat",
                        columnNames = {"screening_id", "seat_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ScreeningSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Screening is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "screening_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_screening_seat_screening")
    )
    private Screening screening;

    @NotNull(message = "Seat is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "seat_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_screening_seat_seat")
    )
    private Seat seat;

    @NotNull(message = "Seat price is required")
    @DecimalMin(
            value = "0.01",
            message = "Seat price must be greater than zero"
    )
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Screening seat status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScreeningSeatStatus status = ScreeningSeatStatus.AVAILABLE;

    @Column(name = "reserved_until")
    private LocalDateTime reservedUntil;

    @Version
    private Long version;
}