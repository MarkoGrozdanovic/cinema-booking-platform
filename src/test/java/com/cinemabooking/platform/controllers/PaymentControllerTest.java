package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.config.SecurityConfig;
import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.enums.PaymentStatus;
import com.cinemabooking.platform.model.request.CreatePaymentRequestDTO;
import com.cinemabooking.platform.model.response.PaymentIntentResponseDTO;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import com.cinemabooking.platform.security.RestSecurityExceptionHandler;
import com.cinemabooking.platform.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import({
        SecurityConfig.class,
        RestSecurityExceptionHandler.class
})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void createPaymentIntent_shouldReturnCreatedForCustomer() throws Exception {
        PaymentIntentResponseDTO response = PaymentIntentResponseDTO.builder()
                .paymentId(7L)
                .bookingId(5L)
                .providerPaymentId("pi_test")
                .clientSecret("pi_test_secret_test")
                .status(PaymentStatus.PENDING)
                .amount(new BigDecimal("1600.00"))
                .currency("rsd")
                .build();

        when(paymentService.createPaymentIntent(
                any(CreatePaymentRequestDTO.class),
                eq(2L)
        )).thenReturn(response);

        mockMvc.perform(post("/api/payments/payment-intents")
                        .with(authentication(customerAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookingId": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(7))
                .andExpect(jsonPath("$.bookingId").value(5))
                .andExpect(jsonPath("$.providerPaymentId").value("pi_test"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(1600.00))
                .andExpect(jsonPath("$.currency").value("rsd"));

        verify(paymentService).createPaymentIntent(
                any(CreatePaymentRequestDTO.class),
                eq(2L)
        );
    }

    @Test
    void createPaymentIntent_shouldRejectMissingBookingId() throws Exception {
        mockMvc.perform(post("/api/payments/payment-intents")
                        .with(authentication(customerAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.bookingId").exists());

        verifyNoInteractions(paymentService);
    }

    @Test
    void createPaymentIntent_shouldReturnUnauthorizedWithoutAuthentication()
            throws Exception {
        mockMvc.perform(post("/api/payments/payment-intents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":5}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paymentService);
    }

    @Test
    void createPaymentIntent_shouldReturnForbiddenForAdmin() throws Exception {
        mockMvc.perform(post("/api/payments/payment-intents")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":5}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_shouldBePublicAndForwardRawPayloadAndSignature()
            throws Exception {
        String payload = "{\"id\":\"evt_test\"}";

        mockMvc.perform(post("/api/payments/webhook")
                        .header("Stripe-Signature", "test-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(paymentService).handleStripeWebhook(
                payload,
                "test-signature"
        );
    }

    @Test
    void webhook_shouldReturnBadRequestWhenSignatureHeaderIsMissing()
            throws Exception {
        mockMvc.perform(post("/api/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"evt_test\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paymentService);
    }

    private UsernamePasswordAuthenticationToken customerAuthentication() {
        AppUser customer = new AppUser();
        customer.setId(2L);
        customer.setEmail("customer@example.com");
        customer.setRole(AppRole.CUSTOMER);
        customer.setActive(true);

        return UsernamePasswordAuthenticationToken.authenticated(
                customer,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        AppUser admin = new AppUser();
        admin.setId(1L);
        admin.setEmail("admin@example.com");
        admin.setRole(AppRole.ADMIN);
        admin.setActive(true);

        return UsernamePasswordAuthenticationToken.authenticated(
                admin,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
