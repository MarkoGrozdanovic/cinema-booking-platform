package com.cinemabooking.platform.model.response;

import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingResponseDTO {

    private Long id;
    private String bookingReference;

    private Long customerId;
    private String customerName;
    private String customerEmail;

    private String movieTitle;
    private String cinemaName;
    private String hallName;
    private LocalDateTime screeningStartTime;

    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;

    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}