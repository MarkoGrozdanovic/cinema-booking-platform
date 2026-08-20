package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.model.request.CreateScreeningRequestDTO;
import com.cinemabooking.platform.model.response.ScreeningResponseDTO;
import com.cinemabooking.platform.service.ScreeningSerivce;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(ScreeningController.class)
class ScreeningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScreeningSerivce screeningService;

    @Test
    void shouldCreateScreening() throws Exception {
        ScreeningResponseDTO response = ScreeningResponseDTO.builder()
                .id(1L)
                .movieId(10L)
                .movieTitle("The Dark Knight")
                .hallId(20L)
                .cinemaName("Central Cinema")
                .startTime(LocalDateTime.of(2099, 8, 20, 20, 0))
                .endTime(LocalDateTime.of(2099, 8, 20, 22, 40))
                .basePrice(new BigDecimal("800.00"))
                .status("SCHEDULED")
                .numberOfSeats(100)
                .build();

        when(screeningService.createScreening(
                any(CreateScreeningRequestDTO.class)
        )).thenReturn(response);

        String requestBody = """
                {
                    "movieId": 10,
                    "hallId": 20,
                    "startTime": "2099-08-20T20:00:00",
                    "basePrice": 800.00
                }
                """;

        mockMvc.perform(post("/api/screenings")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.movieId").value(10))
                .andExpect(jsonPath("$.movieTitle")
                        .value("The Dark Knight"))
                .andExpect(jsonPath("$.hallId").value(20))
                .andExpect(jsonPath("$.cinemaName")
                        .value("Central Cinema"))
                .andExpect(jsonPath("$.basePrice").value(800.00))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.numberOfSeats").value(100));
    }

    @Test
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        String requestBody = """
            {
                "movieId": null,
                "hallId": null,
                "startTime": "2020-08-20T20:00:00",
                "basePrice": -100.00
            }
            """;

        mockMvc.perform(post("/api/screenings")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(screeningService, never())
                .createScreening(any(CreateScreeningRequestDTO.class));
    }

    @Test
    void shouldReturnForbiddenWhenCustomerCreatesScreening()
            throws Exception {

        String requestBody = """
            {
                "movieId": 10,
                "hallId": 20,
                "startTime": "2099-08-20T20:00:00",
                "basePrice": 800.00
            }
            """;

        mockMvc.perform(post("/api/screenings")
                        .with(authentication(customerAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());

        verify(screeningService, never())
                .createScreening(any(CreateScreeningRequestDTO.class));
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        AppUser admin = new AppUser();
        admin.setId(2L);
        admin.setEmail("admin@cinema.com");
        admin.setRole(AppRole.ADMIN);
        admin.setActive(true);

        return UsernamePasswordAuthenticationToken.authenticated(
                admin,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );
    }

    private UsernamePasswordAuthenticationToken customerAuthentication() {
        AppUser customer = new AppUser();
        customer.setId(1L);
        customer.setEmail("customer@cinema.com");
        customer.setRole(AppRole.CUSTOMER);
        customer.setActive(true);

        return UsernamePasswordAuthenticationToken.authenticated(
                customer,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_CUSTOMER")
                )
        );
    }
}