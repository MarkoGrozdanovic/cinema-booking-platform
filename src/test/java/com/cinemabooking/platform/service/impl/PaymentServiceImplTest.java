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
import com.stripe.Stripe;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final String WEBHOOK_SECRET = "whsec_test_secret";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StripeClient stripeClient;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        StripeProperties stripeProperties = new StripeProperties();
        stripeProperties.setSecretKey("sk_test_example");
        stripeProperties.setWebhookSecret(WEBHOOK_SECRET);
        stripeProperties.setCurrency("RSD");

        paymentService = new PaymentServiceImpl(
                paymentRepository,
                bookingRepository,
                stripeClient,
                stripeProperties
        );
    }

    @Test
    void createPaymentIntent_shouldThrowNotFoundWhenBookingIsNotOwned() {
        when(bookingRepository.findByIdAndUserId(5L, 2L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.createPaymentIntent(
                        new CreatePaymentRequestDTO(5L),
                        2L
                )
        );

        assertEquals(
                "Booking with ID 5 was not found",
                exception.getMessage()
        );
        verifyNoInteractions(paymentRepository, stripeClient);
    }

    @Test
    void createPaymentIntent_shouldRejectBookingWithInvalidStatus() {
        Booking booking = validPendingBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByIdAndUserId(5L, 2L))
                .thenReturn(Optional.of(booking));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.createPaymentIntent(
                        new CreatePaymentRequestDTO(5L),
                        2L
                )
        );

        assertEquals(
                "Payment can only be created for a pending payment booking",
                exception.getMessage()
        );
        verifyNoInteractions(paymentRepository, stripeClient);
    }

    @Test
    void createPaymentIntent_shouldRejectExpiredBooking() {
        Booking booking = validPendingBooking();
        booking.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(bookingRepository.findByIdAndUserId(5L, 2L))
                .thenReturn(Optional.of(booking));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.createPaymentIntent(
                        new CreatePaymentRequestDTO(5L),
                        2L
                )
        );

        assertEquals("Booking has expired", exception.getMessage());
        verifyNoInteractions(paymentRepository, stripeClient);
    }

    @Test
    void createPaymentIntent_shouldReuseExistingOpenPayment()
            throws StripeException {
        Booking booking = validPendingBooking();

        Payment existingPayment = payment(
                PaymentStatus.PENDING,
                booking
        );

        PaymentIntent stripeIntent = stripePaymentIntent(
                "pi_test",
                160000L,
                "rsd",
                "pi_test_secret_test"
        );
        stripeIntent.setStatus("requires_payment_method");

        when(bookingRepository.findByIdAndUserId(5L, 2L))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.findByBookingId(5L))
                .thenReturn(Optional.of(existingPayment));

        when(stripeClient
                .v1()
                .paymentIntents()
                .retrieve("pi_test"))
                .thenReturn(stripeIntent);

        PaymentIntentResponseDTO response =
                paymentService.createPaymentIntent(
                        new CreatePaymentRequestDTO(5L),
                        2L
                );

        assertEquals(7L, response.getPaymentId());
        assertEquals(5L, response.getBookingId());
        assertEquals(
                "pi_test",
                response.getProviderPaymentId()
        );
        assertEquals(
                "pi_test_secret_test",
                response.getClientSecret()
        );
        assertEquals(
                PaymentStatus.PENDING,
                response.getStatus()
        );
        assertEquals(
                new BigDecimal("1600.00"),
                response.getAmount()
        );
        assertEquals("rsd", response.getCurrency());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(stripeClient.v1().paymentIntents(), never())
                .create(
                        any(PaymentIntentCreateParams.class),
                        any(RequestOptions.class)
                );
    }

    @Test
    void createPaymentIntent_shouldCreateStripeIntentAndSavePayment()
            throws StripeException {
        Booking booking = validPendingBooking();
        PaymentIntent stripeIntent = stripePaymentIntent(
                "pi_test",
                160000L,
                "rsd",
                "pi_test_secret_test"
        );

        when(bookingRepository.findByIdAndUserId(5L, 2L))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(5L))
                .thenReturn(Optional.empty());
        when(stripeClient.v1().paymentIntents().create(
                any(PaymentIntentCreateParams.class),
                any(RequestOptions.class)
        )).thenReturn(stripeIntent);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    payment.setId(7L);
                    return payment;
                });

        PaymentIntentResponseDTO response =
                paymentService.createPaymentIntent(
                        new CreatePaymentRequestDTO(5L),
                        2L
                );

        assertEquals(7L, response.getPaymentId());
        assertEquals(5L, response.getBookingId());
        assertEquals("pi_test", response.getProviderPaymentId());
        assertEquals("pi_test_secret_test", response.getClientSecret());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("1600.00"), response.getAmount());
        assertEquals("rsd", response.getCurrency());

        ArgumentCaptor<Payment> paymentCaptor =
                ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(booking, savedPayment.getBooking());
        assertEquals("pi_test", savedPayment.getProviderPaymentId());
        assertEquals(PaymentProvider.STRIPE, savedPayment.getProvider());
        assertEquals(PaymentStatus.PENDING, savedPayment.getStatus());
        assertEquals(new BigDecimal("1600.00"), savedPayment.getAmount());
        assertEquals("rsd", savedPayment.getCurrency());
    }

    @Test
    void createPaymentIntent_shouldTranslateStripeFailure()
            throws StripeException {
        Booking booking = validPendingBooking();
        StripeException stripeException = mock(StripeException.class);

        when(bookingRepository.findByIdAndUserId(5L, 2L))
                .thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(5L))
                .thenReturn(Optional.empty());
        when(stripeClient.v1().paymentIntents().create(
                any(PaymentIntentCreateParams.class),
                any(RequestOptions.class)
        )).thenThrow(stripeException);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.createPaymentIntent(
                        new CreatePaymentRequestDTO(5L),
                        2L
                )
        );

        assertEquals(
                "Unable to initialize payment. Please try again",
                exception.getMessage()
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void cancelOpenPaymentForBooking_shouldDoNothingWithoutPayment() {
        when(paymentRepository.findByBookingId(5L))
                .thenReturn(Optional.empty());

        paymentService.cancelOpenPaymentForBooking(5L);

        verifyNoInteractions(stripeClient);
    }

    @Test
    void cancelOpenPaymentForBooking_shouldIgnoreSucceededPayment() {
        Payment payment = payment(PaymentStatus.SUCCEEDED, validPendingBooking());
        when(paymentRepository.findByBookingId(5L))
                .thenReturn(Optional.of(payment));

        paymentService.cancelOpenPaymentForBooking(5L);

        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        verifyNoInteractions(stripeClient);
    }

    @Test
    void cancelOpenPaymentForBooking_shouldCancelStripePayment()
            throws StripeException {
        Payment payment = payment(PaymentStatus.PENDING, validPendingBooking());
        when(paymentRepository.findByBookingId(5L))
                .thenReturn(Optional.of(payment));
        when(stripeClient.v1().paymentIntents().cancel(
                eq("pi_test"),
                any(PaymentIntentCancelParams.class)
        )).thenReturn(new PaymentIntent());

        paymentService.cancelOpenPaymentForBooking(5L);

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());

        ArgumentCaptor<PaymentIntentCancelParams> captor =
                ArgumentCaptor.forClass(PaymentIntentCancelParams.class);
        verify(stripeClient.v1().paymentIntents())
                .cancel(eq("pi_test"), captor.capture());
        assertEquals(
                PaymentIntentCancelParams.CancellationReason.REQUESTED_BY_CUSTOMER,
                captor.getValue().getCancellationReason()
        );
    }

    @Test
    void cancelExpiredBookingPayment_shouldUseAbandonedReason()
            throws StripeException {
        Payment payment = payment(PaymentStatus.FAILED, validPendingBooking());
        when(paymentRepository.findByBookingId(5L))
                .thenReturn(Optional.of(payment));
        when(stripeClient.v1().paymentIntents().cancel(
                eq("pi_test"),
                any(PaymentIntentCancelParams.class)
        )).thenReturn(new PaymentIntent());

        paymentService.cancelExpiredBookingPayment(5L);

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());

        ArgumentCaptor<PaymentIntentCancelParams> captor =
                ArgumentCaptor.forClass(PaymentIntentCancelParams.class);
        verify(stripeClient.v1().paymentIntents())
                .cancel(eq("pi_test"), captor.capture());
        assertEquals(
                PaymentIntentCancelParams.CancellationReason.ABANDONED,
                captor.getValue().getCancellationReason()
        );
    }

    @Test
    void cancelOpenPaymentForBooking_shouldKeepLocalStatusWhenStripeFails()
            throws StripeException {
        Payment payment = payment(PaymentStatus.PENDING, validPendingBooking());
        StripeException stripeException = mock(StripeException.class);
        when(paymentRepository.findByBookingId(5L))
                .thenReturn(Optional.of(payment));
        when(stripeClient.v1().paymentIntents().cancel(
                eq("pi_test"),
                any(PaymentIntentCancelParams.class)
        )).thenThrow(stripeException);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.cancelOpenPaymentForBooking(5L)
        );

        assertEquals("Unable to cancel the payment", exception.getMessage());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    void webhook_shouldRejectInvalidSignature() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.handleStripeWebhook(
                        "{}",
                        "invalid-signature"
                )
        );

        assertEquals(
                "Invalid Stripe webhook signature",
                exception.getMessage()
        );
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void succeededWebhook_shouldConfirmBookingAndSellSeats() throws Exception {
        Booking booking = validPendingBooking();
        ScreeningSeat firstSeat = heldSeat();
        ScreeningSeat secondSeat = heldSeat();
        addItem(booking, firstSeat);
        addItem(booking, secondSeat);
        Payment payment = payment(PaymentStatus.PENDING, booking);

        when(paymentRepository.findByProviderPaymentId("pi_test"))
                .thenReturn(Optional.of(payment));

        String payload = paymentIntentEvent(
                "evt_success",
                "payment_intent.succeeded",
                "succeeded",
                160000L,
                "rsd"
        );

        paymentService.handleStripeWebhook(
                payload,
                signatureFor(payload)
        );

        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(ScreeningSeatStatus.SOLD, firstSeat.getStatus());
        assertEquals(ScreeningSeatStatus.SOLD, secondSeat.getStatus());
        assertNull(firstSeat.getReservedUntil());
        assertNull(secondSeat.getReservedUntil());
    }

    @Test
    void succeededWebhook_shouldBeIdempotent() throws Exception {
        Booking booking = validPendingBooking();
        Payment payment = payment(PaymentStatus.SUCCEEDED, booking);
        when(paymentRepository.findByProviderPaymentId("pi_test"))
                .thenReturn(Optional.of(payment));

        String payload = paymentIntentEvent(
                "evt_duplicate",
                "payment_intent.succeeded",
                "succeeded",
                160000L,
                "rsd"
        );

        paymentService.handleStripeWebhook(payload, signatureFor(payload));

        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getStatus());
    }

    @Test
    void succeededWebhook_shouldRejectAmountMismatch() throws Exception {
        Payment payment = payment(PaymentStatus.PENDING, validPendingBooking());
        when(paymentRepository.findByProviderPaymentId("pi_test"))
                .thenReturn(Optional.of(payment));

        String payload = paymentIntentEvent(
                "evt_amount",
                "payment_intent.succeeded",
                "succeeded",
                999L,
                "rsd"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.handleStripeWebhook(
                        payload,
                        signatureFor(payload)
                )
        );

        assertEquals(
                "Stripe payment amount does not match",
                exception.getMessage()
        );
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    void succeededWebhook_shouldRejectCurrencyMismatch() throws Exception {
        Payment payment = payment(PaymentStatus.PENDING, validPendingBooking());
        when(paymentRepository.findByProviderPaymentId("pi_test"))
                .thenReturn(Optional.of(payment));

        String payload = paymentIntentEvent(
                "evt_currency",
                "payment_intent.succeeded",
                "succeeded",
                160000L,
                "eur"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.handleStripeWebhook(
                        payload,
                        signatureFor(payload)
                )
        );

        assertEquals(
                "Stripe payment currency does not match",
                exception.getMessage()
        );
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    void succeededWebhook_shouldRejectBookingWithInvalidStatus()
            throws Exception {
        Booking booking = validPendingBooking();
        booking.setStatus(BookingStatus.CANCELLED);
        Payment payment = payment(PaymentStatus.PENDING, booking);
        when(paymentRepository.findByProviderPaymentId("pi_test"))
                .thenReturn(Optional.of(payment));

        String payload = paymentIntentEvent(
                "evt_booking_status",
                "payment_intent.succeeded",
                "succeeded",
                160000L,
                "rsd"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.handleStripeWebhook(
                        payload,
                        signatureFor(payload)
                )
        );

        assertEquals(
                "Booking cannot be confirmed in its current status",
                exception.getMessage()
        );
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    void succeededWebhook_shouldRejectSeatThatIsNoLongerHeld()
            throws Exception {
        Booking booking = validPendingBooking();
        ScreeningSeat seat = heldSeat();
        seat.setStatus(ScreeningSeatStatus.AVAILABLE);
        addItem(booking, seat);
        Payment payment = payment(PaymentStatus.PENDING, booking);
        when(paymentRepository.findByProviderPaymentId("pi_test"))
                .thenReturn(Optional.of(payment));

        String payload = paymentIntentEvent(
                "evt_seat_status",
                "payment_intent.succeeded",
                "succeeded",
                160000L,
                "rsd"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentService.handleStripeWebhook(
                        payload,
                        signatureFor(payload)
                )
        );

        assertEquals(
                "One or more booking seats are no longer held",
                exception.getMessage()
        );
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    void succeededWebhook_shouldThrowNotFoundForUnknownProviderPaymentId()
            throws Exception {
        when(paymentRepository.findByProviderPaymentId("pi_test"))
                .thenReturn(Optional.empty());

        String payload = paymentIntentEvent(
                "evt_unknown_payment",
                "payment_intent.succeeded",
                "succeeded",
                160000L,
                "rsd"
        );

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.handleStripeWebhook(
                        payload,
                        signatureFor(payload)
                )
        );

        assertEquals(
                "Payment with provider ID pi_test was not found",
                exception.getMessage()
        );
    }

    @Test
    void failedWebhook_shouldMarkPaymentFailedAndKeepBookingPending()
            throws Exception {
        Booking booking = validPendingBooking();
        Payment payment = payment(PaymentStatus.PENDING, booking);
        when(paymentRepository.findByProviderPaymentId("pi_test"))
                .thenReturn(Optional.of(payment));

        String payload = paymentIntentEvent(
                "evt_failed",
                "payment_intent.payment_failed",
                "requires_payment_method",
                160000L,
                "rsd"
        );

        paymentService.handleStripeWebhook(payload, signatureFor(payload));

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getStatus());
    }

    @Test
    void canceledWebhook_shouldCancelBookingAndReleaseHeldSeats()
            throws Exception {
        Booking booking = validPendingBooking();
        ScreeningSeat seat = heldSeat();
        addItem(booking, seat);
        Payment payment = payment(PaymentStatus.PENDING, booking);
        when(paymentRepository.findByProviderPaymentId("pi_test"))
                .thenReturn(Optional.of(payment));

        String payload = paymentIntentEvent(
                "evt_canceled",
                "payment_intent.canceled",
                "canceled",
                160000L,
                "rsd"
        );

        paymentService.handleStripeWebhook(payload, signatureFor(payload));

        assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(ScreeningSeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getReservedUntil());
    }

    @Test
    void unsupportedWebhook_shouldBeAcknowledgedWithoutDatabaseAccess()
            throws Exception {
        String payload = event(
                "evt_created",
                "payment_intent.created",
                "requires_payment_method",
                160000L,
                "rsd"
        );

        paymentService.handleStripeWebhook(payload, signatureFor(payload));

        verifyNoInteractions(paymentRepository);
    }

    private Booking validPendingBooking() {
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        booking.setTotalPrice(new BigDecimal("1600.00"));
        return booking;
    }

    private Payment payment(PaymentStatus status, Booking booking) {
        return Payment.builder()
                .id(7L)
                .booking(booking)
                .providerPaymentId("pi_test")
                .provider(PaymentProvider.STRIPE)
                .status(status)
                .amount(new BigDecimal("1600.00"))
                .currency("rsd")
                .build();
    }

    private PaymentIntent stripePaymentIntent(
            String id,
            Long amount,
            String currency,
            String clientSecret
    ) {
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId(id);
        paymentIntent.setAmount(amount);
        paymentIntent.setCurrency(currency);
        paymentIntent.setClientSecret(clientSecret);
        return paymentIntent;
    }

    private ScreeningSeat heldSeat() {
        ScreeningSeat seat = new ScreeningSeat();
        seat.setStatus(ScreeningSeatStatus.HELD);
        seat.setReservedUntil(LocalDateTime.now().plusMinutes(5));
        return seat;
    }

    private void addItem(Booking booking, ScreeningSeat seat) {
        BookingItem item = new BookingItem();
        item.setScreeningSeat(seat);
        item.setPrice(new BigDecimal("800.00"));
        booking.addBookingItem(item);
    }

    private String paymentIntentEvent(
            String eventId,
            String type,
            String status,
            long amount,
            String currency
    ) {
        return event(eventId, type, status, amount, currency);
    }

    private String event(
            String eventId,
            String type,
            String status,
            long amount,
            String currency
    ) {
        return """
                {
                  "id": "%s",
                  "object": "event",
                  "api_version": "%s",
                  "created": %d,
                  "data": {
                    "object": {
                      "id": "pi_test",
                      "object": "payment_intent",
                      "amount": %d,
                      "currency": "%s",
                      "status": "%s"
                    }
                  },
                  "livemode": false,
                  "pending_webhooks": 1,
                  "request": {
                    "id": null,
                    "idempotency_key": null
                  },
                  "type": "%s"
                }
                """.formatted(
                eventId,
                Stripe.API_VERSION,
                Instant.now().getEpochSecond(),
                amount,
                currency,
                status,
                type
        );
    }

    private String signatureFor(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        String signedPayload = timestamp + "." + payload;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        ));

        String signature = HexFormat.of().formatHex(
                mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8))
        );

        return "t=" + timestamp + ",v1=" + signature;
    }
}
