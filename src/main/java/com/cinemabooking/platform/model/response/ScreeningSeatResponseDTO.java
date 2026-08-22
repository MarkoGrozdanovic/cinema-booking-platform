package com.cinemabooking.platform.model.response;

import com.cinemabooking.platform.model.enums.ScreeningSeatStatus;
import com.cinemabooking.platform.model.enums.SeatType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ScreeningSeatResponseDTO {

    private Long screeningSeatId;
    private String rowLabel;
    private Integer seatNumber;
    private SeatType seatType;
    private BigDecimal price;
    private ScreeningSeatStatus status;
    private LocalDateTime reservedUntil;
}