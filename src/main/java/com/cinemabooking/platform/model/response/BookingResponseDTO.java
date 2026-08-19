package com.cinemabooking.platform.model.response;

import com.cinemabooking.platform.model.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long id;
    private String bookingReference;
    private String movieTitle;
    private String cinemaName;
    private String hallName;
    private BookingStatus status;
    private LocalDateTime screeningStartTime;
    private LocalDateTime expiresAt;
    private BigDecimal totalPrice;
    private List<BookedSeatResponseDTO> selectedSeats;
}
