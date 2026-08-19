package com.cinemabooking.platform.model.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreeningResponseDTO {

    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long hallId;
    private String cinemaName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
    private String status;
    private Integer numberOfSeats;
}
