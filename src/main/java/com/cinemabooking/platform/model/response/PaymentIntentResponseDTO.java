package com.cinemabooking.platform.model.response;

import com.cinemabooking.platform.model.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIntentResponseDTO {

    private Long paymentId;
    private Long bookingId;
    private String providerPaymentId;
    private String clientSecret;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
}
