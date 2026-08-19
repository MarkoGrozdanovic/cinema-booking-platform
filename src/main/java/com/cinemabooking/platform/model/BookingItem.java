package com.cinemabooking.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "booking_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_screening_seat",
                        columnNames = {
                                "booking_id",
                                "screening_seat_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Booking is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "booking_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_booking_item_booking")
    )
    private Booking booking;

    @NotNull(message = "Screening seat is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "screening_seat_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_booking_item_screening_seat"
            )
    )
    private ScreeningSeat screeningSeat;

    @NotNull(message = "Booking item price is required")
    @DecimalMin(
            value = "0.01",
            message = "Booking item price must be greater than zero"
    )
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}