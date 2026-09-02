package com.cinemabooking.platform.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HallOptionResponseDTO {

    private Long id;
    private String hallName;
    private String cinemaName;
}