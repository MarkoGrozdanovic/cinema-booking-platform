package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.config.SecurityConfig;
import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.request.CreateCinemaRequestDTO;
import com.cinemabooking.platform.model.response.CinemaResponseDTO;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import com.cinemabooking.platform.security.RestSecurityExceptionHandler;
import com.cinemabooking.platform.service.CinemaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;

@WebMvcTest(CinemaController.class)
@Import({
        SecurityConfig.class,
        RestSecurityExceptionHandler.class
})
class CinemaControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CinemaService cinemaService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateCinemaAsAdmin() throws Exception {
        CinemaResponseDTO response =
                CinemaResponseDTO.builder()
                        .id(10L)
                        .name("CineStar Novi Sad")
                        .address("Bulevar oslobođenja 119")
                        .city("Novi Sad")
                        .description("Modern multiplex cinema")
                        .active(true)
                        .build();

        when(cinemaService.createCinema(
                any(CreateCinemaRequestDTO.class)
        )).thenReturn(response);

        String requestBody = """
                {
                  "name": "CineStar Novi Sad",
                  "address": "Bulevar oslobođenja 119",
                  "city": "Novi Sad",
                  "description": "Modern multiplex cinema"
                }
                """;

        mockMvc.perform(
                        post("/api/admin/cinemas")
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
                        .value("CineStar Novi Sad"))
                .andExpect(jsonPath("$.address")
                        .value("Bulevar oslobođenja 119"))
                .andExpect(jsonPath("$.city")
                        .value("Novi Sad"))
                .andExpect(jsonPath("$.description")
                        .value("Modern multiplex cinema"))
                .andExpect(jsonPath("$.active")
                        .value(true));

        verify(cinemaService).createCinema(
                any(CreateCinemaRequestDTO.class)
        );
    }

    @Test
    void shouldRejectInvalidCinemaRequest()
            throws Exception {
        String requestBody = """
                {
                  "name": "",
                  "address": "",
                  "city": "",
                  "description": ""
                }
                """;

        mockMvc.perform(
                        post("/api/admin/cinemas")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(cinemaService, never()).createCinema(
                any(CreateCinemaRequestDTO.class)
        );
    }

    @Test
    void shouldReturnCinemasForAdmin()
            throws Exception {
        CinemaResponseDTO cinema =
                CinemaResponseDTO.builder()
                        .id(10L)
                        .name("CineStar Novi Sad")
                        .address("Bulevar oslobođenja 119")
                        .city("Novi Sad")
                        .description("Modern multiplex cinema")
                        .active(true)
                        .build();

        when(cinemaService.getAllCinemas())
                .thenReturn(List.of(cinema));

        mockMvc.perform(
                        get("/api/admin/cinemas")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(10))
                .andExpect(jsonPath("$[0].name")
                        .value("CineStar Novi Sad"))
                .andExpect(jsonPath("$[0].city")
                        .value("Novi Sad"))
                .andExpect(jsonPath("$[0].active")
                        .value(true));

        verify(cinemaService).getAllCinemas();
    }

    @Test
    void shouldReturnForbiddenForCustomer()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/cinemas")
                                .with(authentication(
                                        customerAuthentication()
                                ))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(cinemaService);
    }

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/cinemas")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cinemaService);
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

    @Test
    void shouldUpdateCinemaStatusAsAdmin()
            throws Exception {
        CinemaResponseDTO response =
                CinemaResponseDTO.builder()
                        .id(10L)
                        .name("Central Cinema")
                        .address("Main Street 1")
                        .city("Belgrade")
                        .active(false)
                        .build();

        when(cinemaService.updateCinemaStatus(
                10L,
                false
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/admin/cinemas/10/status")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "active": false
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(10))
                .andExpect(jsonPath("$.active")
                        .value(false));

        verify(cinemaService).updateCinemaStatus(
                10L,
                false
        );
    }

    @Test
    void shouldRejectCinemaStatusWithoutActiveValue()
            throws Exception {
        mockMvc.perform(
                        put("/api/admin/cinemas/10/status")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(status().isBadRequest());

        verify(cinemaService, never())
                .updateCinemaStatus(
                        anyLong(),
                        anyBoolean()
                );
    }

    @Test
    void shouldReturnForbiddenWhenCustomerUpdatesCinemaStatus()
            throws Exception {
        mockMvc.perform(
                        put("/api/admin/cinemas/10/status")
                                .with(authentication(
                                        customerAuthentication()
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "active": false
                                    }
                                    """)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(cinemaService);
    }
}