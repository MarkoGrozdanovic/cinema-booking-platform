package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.config.SecurityConfig;
import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.enums.HallType;
import com.cinemabooking.platform.model.request.CreateHallRequestDTO;
import com.cinemabooking.platform.model.response.HallResponseDTO;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import com.cinemabooking.platform.security.RestSecurityExceptionHandler;
import com.cinemabooking.platform.service.HallService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HallController.class)
@Import({
        SecurityConfig.class,
        RestSecurityExceptionHandler.class
})
class HallControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private HallService hallService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateHallAsAdmin() throws Exception {
        HallResponseDTO response =
                HallResponseDTO.builder()
                        .id(10L)
                        .name("Hall 2")
                        .hallType(HallType.STANDARD)
                        .cinemaId(1L)
                        .cinemaName("Central Cinema")
                        .active(true)
                        .numberOfSeats(18)
                        .build();

        when(hallService.createHall(
                any(CreateHallRequestDTO.class)
        )).thenReturn(response);

        String requestBody = """
                {
                  "cinemaId": 1,
                  "name": "Hall 2",
                  "hallType": "STANDARD",
                  "rows": [
                    {
                      "rowLabel": "A",
                      "numberOfSeats": 10,
                      "seatType": "STANDARD"
                    },
                    {
                      "rowLabel": "B",
                      "numberOfSeats": 8,
                      "seatType": "VIP"
                    }
                  ]
                }
                """;

        mockMvc.perform(
                        post("/api/admin/halls")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(10))
                .andExpect(jsonPath("$.name")
                        .value("Hall 2"))
                .andExpect(jsonPath("$.hallType")
                        .value("STANDARD"))
                .andExpect(jsonPath("$.cinemaId")
                        .value(1))
                .andExpect(jsonPath("$.cinemaName")
                        .value("Central Cinema"))
                .andExpect(jsonPath("$.active")
                        .value(true))
                .andExpect(jsonPath("$.numberOfSeats")
                        .value(18));

        verify(hallService).createHall(
                any(CreateHallRequestDTO.class)
        );
    }

    @Test
    void shouldRejectInvalidHallRequest()
            throws Exception {
        String requestBody = """
                {
                  "cinemaId": null,
                  "name": "",
                  "hallType": null,
                  "rows": []
                }
                """;

        mockMvc.perform(
                        post("/api/admin/halls")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(hallService, never()).createHall(
                any(CreateHallRequestDTO.class)
        );
    }

    @Test
    void shouldRejectInvalidSeatRow()
            throws Exception {
        String requestBody = """
                {
                  "cinemaId": 1,
                  "name": "Hall 2",
                  "hallType": "STANDARD",
                  "rows": [
                    {
                      "rowLabel": "123",
                      "numberOfSeats": 0,
                      "seatType": null
                    }
                  ]
                }
                """;

        mockMvc.perform(
                        post("/api/admin/halls")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(hallService, never()).createHall(
                any(CreateHallRequestDTO.class)
        );
    }

    @Test
    void shouldReturnHallsForAdmin()
            throws Exception {
        HallResponseDTO hall =
                HallResponseDTO.builder()
                        .id(10L)
                        .name("Hall 2")
                        .hallType(HallType.IMAX)
                        .cinemaId(1L)
                        .cinemaName("Central Cinema")
                        .active(true)
                        .numberOfSeats(100)
                        .build();

        when(hallService.getAllHalls())
                .thenReturn(List.of(hall));

        mockMvc.perform(
                        get("/api/admin/halls")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(10))
                .andExpect(jsonPath("$[0].name")
                        .value("Hall 2"))
                .andExpect(jsonPath("$[0].hallType")
                        .value("IMAX"))
                .andExpect(jsonPath("$[0].cinemaName")
                        .value("Central Cinema"))
                .andExpect(jsonPath("$[0].numberOfSeats")
                        .value(100));

        verify(hallService).getAllHalls();
    }

    @Test
    void shouldReturnForbiddenForCustomer()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/halls")
                                .with(authentication(
                                        customerAuthentication()
                                ))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(hallService);
    }

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/halls")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(hallService);
    }

    private UsernamePasswordAuthenticationToken
    adminAuthentication() {
        AppUser admin = new AppUser();
        admin.setId(2L);
        admin.setEmail("admin@cinema.com");
        admin.setRole(AppRole.ADMIN);
        admin.setActive(true);

        return UsernamePasswordAuthenticationToken
                .authenticated(
                        admin,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"
                                )
                        )
                );
    }

    private UsernamePasswordAuthenticationToken
    customerAuthentication() {
        AppUser customer = new AppUser();
        customer.setId(1L);
        customer.setEmail("customer@cinema.com");
        customer.setRole(AppRole.CUSTOMER);
        customer.setActive(true);

        return UsernamePasswordAuthenticationToken
                .authenticated(
                        customer,
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_CUSTOMER"
                                )
                        )
                );
    }
}