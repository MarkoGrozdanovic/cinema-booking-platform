package com.cinemabooking.platform.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCinemaRequestDTO {

    @NotBlank(message = "Cinema name is required")
    @Size(
            max = 100,
            message = "Cinema name must not exceed 100 characters"
    )
    private String name;

    @NotBlank(message = "Address is required")
    @Size(
            max = 200,
            message = "Address must not exceed 200 characters"
    )
    private String address;

    @NotBlank(message = "City is required")
    @Size(
            max = 100,
            message = "City must not exceed 100 characters"
    )
    private String city;

    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters"
    )
    private String description;
}