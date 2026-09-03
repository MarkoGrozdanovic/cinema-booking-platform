package com.cinemabooking.platform.model.request;

import com.cinemabooking.platform.model.enums.SeatType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeatRowRequestDTO {

    @NotBlank(message = "Row label is required")
    @Pattern(
            regexp = "[A-Za-z]{1,2}",
            message = "Row label must contain one or two letters"
    )
    private String rowLabel;

    @Min(
            value = 1,
            message = "A row must contain at least one seat"
    )
    @Max(
            value = 50,
            message = "A row cannot contain more than 50 seats"
    )
    private int numberOfSeats;

    @NotNull(message = "Seat type is required")
    private SeatType seatType;
}