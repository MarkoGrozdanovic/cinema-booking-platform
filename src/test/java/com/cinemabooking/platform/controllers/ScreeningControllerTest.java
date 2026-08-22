package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.config.SecurityConfig;
import com.cinemabooking.platform.exceptions.ResourceNotFoundException;
import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.enums.ScreeningSeatStatus;
import com.cinemabooking.platform.model.enums.SeatType;
import com.cinemabooking.platform.model.request.CreateScreeningRequestDTO;
import com.cinemabooking.platform.model.response.ScreeningResponseDTO;
import com.cinemabooking.platform.model.response.ScreeningSeatResponseDTO;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import com.cinemabooking.platform.security.RestSecurityExceptionHandler;
import com.cinemabooking.platform.service.ScreeningSerivce;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScreeningController.class)
@Import({
        SecurityConfig.class,
        RestSecurityExceptionHandler.class
})
class ScreeningControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

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
                        .with(authentication(adminAuthentication()))
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

    @Test
    void shouldReturnScreeningSeatMap() throws Exception {
        ScreeningSeatResponseDTO seat =
                ScreeningSeatResponseDTO.builder()
                        .screeningSeatId(10L)
                        .rowLabel("A")
                        .seatNumber(1)
                        .seatType(SeatType.VIP)
                        .price(new BigDecimal("1200.00"))
                        .status(ScreeningSeatStatus.AVAILABLE)
                        .reservedUntil(null)
                        .build();

        when(screeningService.getScreeningSeats(2L))
                .thenReturn(List.of(seat));

        mockMvc.perform(get("/api/screenings/2/seats")
                        .with(authentication(
                                customerAuthentication()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].screeningSeatId")
                        .value(10))
                .andExpect(jsonPath("$[0].rowLabel")
                        .value("A"))
                .andExpect(jsonPath("$[0].seatNumber")
                        .value(1))
                .andExpect(jsonPath("$[0].seatType")
                        .value("VIP"))
                .andExpect(jsonPath("$[0].price")
                        .value(1200.00))
                .andExpect(jsonPath("$[0].status")
                        .value("AVAILABLE"));
    }

    @Test
    void shouldReturnUnauthorizedWhenSeatMapRequestHasNoAuthentication()
            throws Exception {

        mockMvc.perform(get("/api/screenings/2/seats"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication is required"));

        verifyNoInteractions(screeningService);
    }

    @Test
    void shouldReturnNotFoundWhenScreeningDoesNotExist()
            throws Exception {

        when(screeningService.getScreeningSeats(999L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Screening with ID 999 was not found"
                        )
                );

        mockMvc.perform(get("/api/screenings/999/seats")
                        .with(authentication(
                                customerAuthentication()
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Screening with ID 999 was not found"));
    }
}