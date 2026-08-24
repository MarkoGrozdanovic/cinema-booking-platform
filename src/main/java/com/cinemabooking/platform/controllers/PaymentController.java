package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.request.CreatePaymentRequestDTO;
import com.cinemabooking.platform.model.response.PaymentIntentResponseDTO;
import com.cinemabooking.platform.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payment-intents")
    public ResponseEntity<PaymentIntentResponseDTO> createPaymentIntent(
            @Valid @RequestBody CreatePaymentRequestDTO request,
            @AuthenticationPrincipal AppUser authenticatedUser
    ) {
        PaymentIntentResponseDTO response =
                paymentService.createPaymentIntent(
                        request,
                        authenticatedUser.getId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        paymentService.handleStripeWebhook(
                payload,
                signature
        );

        return ResponseEntity.ok().build();
    }

}
