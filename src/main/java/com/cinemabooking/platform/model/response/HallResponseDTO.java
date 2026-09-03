package com.cinemabooking.platform.model.response;

import com.cinemabooking.platform.model.enums.HallType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HallResponseDTO {

    private Long id;
    private String name;
    private HallType hallType;
    private Long cinemaId;
    private String cinemaName;
    private boolean active;
    private int numberOfSeats;
}