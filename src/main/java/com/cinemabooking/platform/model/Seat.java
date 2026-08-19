package com.cinemabooking.platform.model;

import com.cinemabooking.platform.model.enums.SeatType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "hall_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_position_hall",
                        columnNames = {"row_label", "seat_number", "hall_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Row label is required")
    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;

    @Min(value = 1, message = "Seat number must be greater than zero")
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 30)
    private SeatType seatType;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "hall_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_seat_hall")
    )
    private Hall hall;
}