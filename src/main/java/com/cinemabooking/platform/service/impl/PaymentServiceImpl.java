package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.config.StripeProperties;
import com.cinemabooking.platform.exceptions.BusinessException;
import com.cinemabooking.platform.exceptions.ResourceNotFoundException;
import com.cinemabooking.platform.model.Booking;
import com.cinemabooking.platform.model.BookingItem;
import com.cinemabooking.platform.model.Payment;
import com.cinemabooking.platform.model.ScreeningSeat;
import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.enums.PaymentProvider;
import com.cinemabooking.platform.model.enums.PaymentStatus;
import com.cinemabooking.platform.model.enums.ScreeningSeatStatus;
import com.cinemabooking.platform.model.request.CreatePaymentRequestDTO;
import com.cinemabooking.platform.model.response.PaymentIntentResponseDTO;
import com.cinemabooking.platform.repositories.BookingRepository;
import com.cinemabooking.platform.repositories.PaymentRepository;
import com.cinemabooking.platform.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.StripeClient;
import java.util.Locale;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCancelParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.stripe.param.PaymentIntentCreateParams;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;

    @Override
    @Transactional
    public PaymentIntentResponseDTO createPaymentIntent(
            CreatePaymentRequestDTO request,
            Long authenticatedUserId
    ) {
        Booking booking = findOwnedBooking(
                request.getBookingId(),
                authenticatedUserId
        );

        validateBookingForPayment(booking);

        Payment existingPayment = paymentRepository
                .findByBookingId(booking.getId())
                .orElse(null);

        if (existingPayment != null) {
            return reuseExistingPayment(existingPayment);
        }

        PaymentIntentCreateParams params =
                buildPaymentIntentParams(
                        booking,
                        authenticatedUserId
                );

        PaymentIntent stripePaymentIntent =
                createStripePaymentIntent(booking, params);

        Payment savedPayment =
                savePayment(booking, stripePaymentIntent);

        return toPaymentIntentResponse(
                savedPayment,
                stripePaymentIntent
        );
    }

    @Override
    @Transactional
    public void handleStripeWebhook(
            String payload,
            String signature
    ) {
        Event event = constructStripeEvent(
                payload,
                signature
        );

        try {
            log.info(
                    "Processing Stripe event: id={}, type={}",
                    event.getId(),
                    event.getType()
            );

            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    PaymentIntent paymentIntent =
                            extractPaymentIntent(event);

                    handleSuccessfulPayment(paymentIntent);
                }

                case "payment_intent.payment_failed" -> {
                    PaymentIntent paymentIntent =
                            extractPaymentIntent(event);

                    handleFailedPayment(paymentIntent);
                }

                case "payment_intent.canceled" -> {
                    PaymentIntent paymentIntent =
                            extractPaymentIntent(event);

                    handleCanceledPayment(paymentIntent);
                }

                default -> log.info(
                        "Ignoring unsupported Stripe event: {}",
                        event.getType()
                );
            }
        } catch (RuntimeException exception) {
            log.error(
                    "Stripe webhook failed: eventId={}, type={}, message={}",
                    event.getId(),
                    event.getType(),
                    exception.getMessage(),
                    exception
            );

            throw exception;
        }
    }

    @Override
    @Transactional
    public void cancelOpenPaymentForBooking(Long bookingId) {
        cancelPaymentForBooking(
                bookingId,
                PaymentIntentCancelParams
                        .CancellationReason
                        .REQUESTED_BY_CUSTOMER
        );
    }

    @Override
    @Transactional
    public void cancelExpiredBookingPayment(Long bookingId) {
        cancelPaymentForBooking(
                bookingId,
                PaymentIntentCancelParams
                        .CancellationReason
                        .ABANDONED
        );
    }

    private void cancelPaymentForBooking(
            Long bookingId,
            PaymentIntentCancelParams.CancellationReason reason
    ) {
        Payment payment = paymentRepository
                .findByBookingId(bookingId)
                .orElse(null);

        if (payment == null) {
            return;
        }

        if (payment.getStatus() == PaymentStatus.SUCCEEDED
                || payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }

        cancelStripePaymentIntent(payment, reason);

        payment.setStatus(PaymentStatus.CANCELLED);
    }

    private void cancelStripePaymentIntent(
            Payment payment,
            PaymentIntentCancelParams.CancellationReason reason
    ) {
        PaymentIntentCancelParams params =
                PaymentIntentCancelParams.builder()
                        .setCancellationReason(reason)
                        .build();

        try {
            stripeClient
                    .v1()
                    .paymentIntents()
                    .cancel(
                            payment.getProviderPaymentId(),
                            params
                    );
        } catch (StripeException exception) {
            log.error(
                    "Failed to cancel Stripe Payment Intent: providerPaymentId={}",
                    payment.getProviderPaymentId(),
                    exception
            );

            throw new BusinessException(
                    "Unable to cancel the payment"
            );
        }
    }

    private void handleCanceledPayment(
            PaymentIntent stripePaymentIntent
    ) {
        Payment payment = paymentRepository
                .findByProviderPaymentId(
                        stripePaymentIntent.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment with provider ID "
                                + stripePaymentIntent.getId()
                                + " was not found"
                ));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return;
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }

        payment.setStatus(PaymentStatus.CANCELLED);

        Booking booking = payment.getBooking();

        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            booking.setStatus(BookingStatus.CANCELLED);
            releaseHeldSeats(booking);
        }
    }

    private void releaseHeldSeats(Booking booking) {
        for (BookingItem item : booking.getBookingItems()) {
            ScreeningSeat seat = item.getScreeningSeat();

            if (seat.getStatus() == ScreeningSeatStatus.HELD) {
                seat.setStatus(ScreeningSeatStatus.AVAILABLE);
                seat.setReservedUntil(null);
            }
        }
    }

    private void  handleFailedPayment(
            PaymentIntent stripePaymentIntent
    ) {
        Payment payment = paymentRepository
                .findByProviderPaymentId(
                        stripePaymentIntent.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment with provider ID "
                                + stripePaymentIntent.getId()
                                + " was not found"
                ));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED
                || payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
    }

    private Event constructStripeEvent(
            String payload,
            String signature
    ) {
        try {
            return Webhook.constructEvent(
                    payload,
                    signature,
                    stripeProperties.getWebhookSecret()
            );
        } catch (SignatureVerificationException exception) {
            throw new BusinessException(
                    "Invalid Stripe webhook signature"
            );
        }
    }

    private PaymentIntent extractPaymentIntent(Event event) {
        EventDataObjectDeserializer deserializer =
                event.getDataObjectDeserializer();

        StripeObject stripeObject =
                deserializer.getObject().orElse(null);

        if (stripeObject == null) {
            log.warn(
                    "Stripe event API version {} does not match SDK version {}. Using unsafe deserialization",
                    event.getApiVersion(),
                    Stripe.API_VERSION
            );

            try {
                stripeObject = deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException exception) {
                throw new BusinessException(
                        "Unable to deserialize Stripe Payment Intent"
                );
            }
        }

        if (!(stripeObject instanceof PaymentIntent paymentIntent)) {
            throw new BusinessException(
                    "Stripe event does not contain a Payment Intent"
            );
        }

        return paymentIntent;
    }

    private void handleSuccessfulPayment(
            PaymentIntent stripePaymentIntent
    ) {
        Payment payment = paymentRepository
                .findByProviderPaymentId(
                        stripePaymentIntent.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment with provider ID "
                                + stripePaymentIntent.getId()
                                + " was not found"
                ));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return;
        }

        validateStripePayment(payment, stripePaymentIntent);

        Booking booking = payment.getBooking();

        validateBookingCanBeConfirmed(booking);
        validateHeldSeats(booking);

        payment.setStatus(PaymentStatus.SUCCEEDED);
        booking.setStatus(BookingStatus.CONFIRMED);

        markSeatsAsSold(booking);
    }

    private void validateStripePayment(
            Payment payment,
            PaymentIntent stripePaymentIntent
    ) {
        long expectedAmount = payment.getAmount()
                .movePointRight(2)
                .longValueExact();

        if (!Objects.equals(
                stripePaymentIntent.getAmount(),
                expectedAmount
        )) {
            throw new BusinessException(
                    "Stripe payment amount does not match"
            );
        }

        if (!payment.getCurrency().equalsIgnoreCase(
                stripePaymentIntent.getCurrency()
        )) {
            throw new BusinessException(
                    "Stripe payment currency does not match"
            );
        }
    }

    private void validateBookingCanBeConfirmed(
            Booking booking
    ) {
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException(
                    "Booking cannot be confirmed in its current status"
            );
        }
    }

    private void validateHeldSeats(Booking booking) {
        for (BookingItem item : booking.getBookingItems()) {
            if (item.getScreeningSeat().getStatus()
                    != ScreeningSeatStatus.HELD) {
                throw new BusinessException(
                        "One or more booking seats are no longer held"
                );
            }
        }
    }

    private void markSeatsAsSold(Booking booking) {
        for (BookingItem item : booking.getBookingItems()) {
            ScreeningSeat seat = item.getScreeningSeat();

            seat.setStatus(ScreeningSeatStatus.SOLD);
            seat.setReservedUntil(null);
        }
    }

    private Booking findOwnedBooking(
            Long bookingId,
            Long authenticatedUserId
    ) {
        return bookingRepository
                .findByIdAndUserId(
                        bookingId,
                        authenticatedUserId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking with ID " + bookingId
                                        + " was not found"
                        )
                );
    }



    private void validateBookingForPayment(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException(
                    "Payment can only be created for a pending payment booking"
            );
        }

        if (!booking.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException(
                    "Booking has expired"
            );
        }
    }

    private PaymentIntentCreateParams buildPaymentIntentParams(
            Booking booking,
            Long authenticatedUserId
    ) {
        long amountInMinorUnits = booking.getTotalPrice()
                .movePointRight(2)
                .longValueExact();

        return PaymentIntentCreateParams.builder()
                .setAmount(amountInMinorUnits)
                .setCurrency(
                        stripeProperties.getCurrency()
                                .toLowerCase(Locale.ROOT)
                )
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods
                                .builder()
                                .setEnabled(true)
                                .setAllowRedirects(
                                        PaymentIntentCreateParams
                                                .AutomaticPaymentMethods
                                                .AllowRedirects
                                                .NEVER
                                )
                                .build()
                )
                .putMetadata(
                        "bookingId",
                        booking.getId().toString()
                )
                .putMetadata(
                        "userId",
                        authenticatedUserId.toString()
                )
                .build();
    }

    private PaymentIntent createStripePaymentIntent(
            Booking booking,
            PaymentIntentCreateParams params
    ) {
        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(
                        "booking-payment-" + booking.getId()
                )
                .build();

        try {
            return stripeClient
                    .v1()
                    .paymentIntents()
                    .create(params, requestOptions);
        } catch (StripeException exception) {
            log.error(
                    "Stripe Payment Intent creation failed",
                    exception
            );
            throw new BusinessException(
                    "Unable to initialize payment. Please try again"
            );
        }
    }

    private Payment savePayment(
            Booking booking,
            PaymentIntent stripePaymentIntent
    ) {
        Payment payment = Payment.builder()
                .booking(booking)
                .providerPaymentId(stripePaymentIntent.getId())
                .provider(PaymentProvider.STRIPE)
                .status(PaymentStatus.PENDING)
                .amount(booking.getTotalPrice())
                .currency(stripePaymentIntent.getCurrency())
                .build();

        return paymentRepository.save(payment);
    }

    private PaymentIntentResponseDTO toPaymentIntentResponse(
            Payment payment,
            PaymentIntent stripePaymentIntent
    ) {
        return PaymentIntentResponseDTO.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBooking().getId())
                .providerPaymentId(
                        stripePaymentIntent.getId()
                )
                .clientSecret(
                        stripePaymentIntent.getClientSecret()
                )
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .build();
    }

    private PaymentIntentResponseDTO reuseExistingPayment(
            Payment payment
    ) {
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            throw new BusinessException(
                    "Payment has already been completed"
            );
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException(
                    "Payment has already been cancelled"
            );
        }

        PaymentIntent stripePaymentIntent =
                retrieveStripePaymentIntent(payment);

        if ("succeeded".equals(stripePaymentIntent.getStatus())) {
            throw new BusinessException(
                    "Payment has already been completed"
            );
        }

        if ("canceled".equals(stripePaymentIntent.getStatus())) {
            throw new BusinessException(
                    "Payment has already been cancelled"
            );
        }

        if ("processing".equals(stripePaymentIntent.getStatus())) {
            throw new BusinessException(
                    "Payment is currently being processed"
            );
        }

        return toPaymentIntentResponse(
                payment,
                stripePaymentIntent
        );
    }

    private PaymentIntent retrieveStripePaymentIntent(
            Payment payment
    ) {
        try {
            return stripeClient
                    .v1()
                    .paymentIntents()
                    .retrieve(
                            payment.getProviderPaymentId()
                    );
        } catch (StripeException exception) {
            log.error(
                    "Failed to retrieve Stripe Payment Intent: providerPaymentId={}",
                    payment.getProviderPaymentId(),
                    exception
            );

            throw new BusinessException(
                    "Unable to initialize payment. Please try again"
            );
        }
    }
}
