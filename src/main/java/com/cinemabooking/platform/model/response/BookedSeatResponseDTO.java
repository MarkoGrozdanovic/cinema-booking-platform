package com.cinemabooking.platform.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookedSeatResponseDTO {
    private Long screeningSeatId;
    private String rowLabel;
    private Integer seatNumber;
    private String seatType;
    private BigDecimal price;
}
