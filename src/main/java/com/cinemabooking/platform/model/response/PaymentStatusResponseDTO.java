package com.cinemabooking.platform.model.response;

import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusResponseDTO {

    private Long bookingId;
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
}