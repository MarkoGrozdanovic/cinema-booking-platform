package com.cinemabooking.platform.service;

import com.cinemabooking.platform.model.enums.PaymentCancellationOutcome;
import com.cinemabooking.platform.model.request.CreatePaymentRequestDTO;
import com.cinemabooking.platform.model.response.PaymentIntentResponseDTO;
import com.cinemabooking.platform.model.response.PaymentStatusResponseDTO;

public interface PaymentService {
    PaymentIntentResponseDTO createPaymentIntent(
            CreatePaymentRequestDTO request,
            Long authenticatedUserId
    );
    void handleStripeWebhook(
            String payload,
            String signature
    );

    PaymentCancellationOutcome cancelOpenPaymentForBooking(
            Long bookingId
    );

    PaymentCancellationOutcome cancelExpiredBookingPayment(
            Long bookingId
    );

    PaymentStatusResponseDTO getPaymentStatus(
            Long bookingId,
            Long authenticatedUserId
    );
}
