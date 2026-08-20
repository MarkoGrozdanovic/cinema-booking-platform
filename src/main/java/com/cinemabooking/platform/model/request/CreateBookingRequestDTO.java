package com.cinemabooking.platform.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequestDTO {

    @NotNull(message = "Screening ID is required")
    private Long screeningId;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(
            min = 1,
            max = 8,
            message = "You must select between 1 and 8 seats"
    )
    private List<Long> screeningSeatIds;
}
