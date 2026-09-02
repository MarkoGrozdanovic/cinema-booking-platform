package com.cinemabooking.platform.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMovieStatusRequestDTO {

    @NotNull(message = "Active status is required")
    private Boolean active;
}