package com.cinemabooking.platform.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaResponseDTO {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String description;
    private boolean active;
}