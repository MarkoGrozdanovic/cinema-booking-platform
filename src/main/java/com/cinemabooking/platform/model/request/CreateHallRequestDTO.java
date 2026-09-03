package com.cinemabooking.platform.model.request;

import com.cinemabooking.platform.model.enums.HallType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateHallRequestDTO {

    @NotNull(message = "Cinema ID is required")
    @Positive(message = "Cinema ID must be positive")
    private Long cinemaId;

    @NotBlank(message = "Hall name is required")
    @Size(
            max = 50,
            message = "Hall name must not exceed 50 characters"
    )
    private String name;

    @NotNull(message = "Hall type is required")
    private HallType hallType;

    @Valid
    @NotEmpty(message = "At least one seat row is required")
    @Size(
            max = 26,
            message = "A hall cannot contain more than 26 rows"
    )
    private List<CreateSeatRowRequestDTO> rows;
}