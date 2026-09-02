package com.cinemabooking.platform.controllers;

import com.cinemabooking.platform.config.SecurityConfig;
import com.cinemabooking.platform.model.AppUser;
import com.cinemabooking.platform.model.enums.AgeRating;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.enums.Genre;
import com.cinemabooking.platform.model.request.CreateMovieRequestDTO;
import com.cinemabooking.platform.model.response.MovieResponseDTO;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.security.JwtService;
import com.cinemabooking.platform.security.RestSecurityExceptionHandler;
import com.cinemabooking.platform.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
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

@WebMvcTest(MovieController.class)
@Import({
        SecurityConfig.class,
        RestSecurityExceptionHandler.class
})
class MovieControllerTest  {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private MovieService movieService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateMovieAsAdmin() throws Exception {
        MovieResponseDTO response =
                MovieResponseDTO.builder()
                        .id(10L)
                        .title("Inception")
                        .description(
                                "Dream-sharing thriller"
                        )
                        .durationMinutes(148)
                        .releaseDate(
                                LocalDate.of(2010, 7, 16)
                        )
                        .ageRating(
                                AgeRating.TWELVE_PLUS
                        )
                        .genre(
                                Genre.SCIENCE_FICTION
                        )
                        .language("English")
                        .director("Christopher Nolan")
                        .posterUrl(
                                "https://example.com/poster.jpg"
                        )
                        .active(true)
                        .build();

        when(movieService.createMovie(
                any(CreateMovieRequestDTO.class)
        )).thenReturn(response);

        String requestBody = """
                {
                  "title": "Inception",
                  "description": "Dream-sharing thriller",
                  "durationMinutes": 148,
                  "releaseDate": "2010-07-16",
                  "ageRating": "TWELVE_PLUS",
                  "genre": "SCIENCE_FICTION",
                  "language": "English",
                  "director": "Christopher Nolan",
                  "posterUrl": "https://example.com/poster.jpg",
                  "trailerUrl": null
                }
                """;

        mockMvc.perform(
                        post("/api/admin/movies")
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
                .andExpect(jsonPath("$.title")
                        .value("Inception"))
                .andExpect(jsonPath("$.durationMinutes")
                        .value(148))
                .andExpect(jsonPath("$.ageRating")
                        .value("TWELVE_PLUS"))
                .andExpect(jsonPath("$.genre")
                        .value("SCIENCE_FICTION"))
                .andExpect(jsonPath("$.active")
                        .value(true));

        verify(movieService).createMovie(
                any(CreateMovieRequestDTO.class)
        );
    }

    @Test
    void shouldRejectInvalidMovieRequest()
            throws Exception {
        String requestBody = """
                {
                  "title": "",
                  "description": "",
                  "durationMinutes": 0,
                  "releaseDate": null,
                  "ageRating": null,
                  "genre": null,
                  "language": "",
                  "director": ""
                }
                """;

        mockMvc.perform(
                        post("/api/admin/movies")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verify(movieService, never()).createMovie(
                any(CreateMovieRequestDTO.class)
        );
    }

    @Test
    void shouldReturnMoviesForAdmin() throws Exception {
        MovieResponseDTO movie =
                MovieResponseDTO.builder()
                        .id(10L)
                        .title("Inception")
                        .durationMinutes(148)
                        .releaseDate(
                                LocalDate.of(2010, 7, 16)
                        )
                        .ageRating(
                                AgeRating.TWELVE_PLUS
                        )
                        .genre(
                                Genre.SCIENCE_FICTION
                        )
                        .language("English")
                        .director("Christopher Nolan")
                        .active(true)
                        .build();

        when(movieService.getAllMovies())
                .thenReturn(List.of(movie));

        mockMvc.perform(
                        get("/api/admin/movies")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(10))
                .andExpect(jsonPath("$[0].title")
                        .value("Inception"))
                .andExpect(jsonPath("$[0].active")
                        .value(true));

        verify(movieService).getAllMovies();
    }

    @Test
    void shouldReturnForbiddenForCustomer()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/movies")
                                .with(authentication(
                                        customerAuthentication()
                                ))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(movieService);
    }

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication()
            throws Exception {
        mockMvc.perform(
                        get("/api/admin/movies")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(movieService);
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
    void shouldUpdateMovieStatusAsAdmin()
            throws Exception {
        MovieResponseDTO response =
                MovieResponseDTO.builder()
                        .id(10L)
                        .title("Inception")
                        .durationMinutes(148)
                        .releaseDate(
                                LocalDate.of(2010, 7, 16)
                        )
                        .ageRating(
                                AgeRating.TWELVE_PLUS
                        )
                        .genre(
                                Genre.SCIENCE_FICTION
                        )
                        .language("English")
                        .director("Christopher Nolan")
                        .active(false)
                        .build();

        when(movieService.updateMovieStatus(
                10L,
                false
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/admin/movies/10/status")
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
                .andExpect(jsonPath("$.title")
                        .value("Inception"))
                .andExpect(jsonPath("$.active")
                        .value(false));

        verify(movieService).updateMovieStatus(
                10L,
                false
        );
    }

    @Test
    void shouldRejectMovieStatusWithoutActiveValue()
            throws Exception {
        mockMvc.perform(
                        put("/api/admin/movies/10/status")
                                .with(authentication(
                                        adminAuthentication()
                                ))
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(status().isBadRequest());

        verify(movieService, never())
                .updateMovieStatus(
                        any(Long.class),
                        any(Boolean.class)
                );
    }

    @Test
    void shouldReturnForbiddenWhenCustomerUpdatesMovieStatus()
            throws Exception {
        mockMvc.perform(
                        put("/api/admin/movies/10/status")
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

        verifyNoInteractions(movieService);
    }
}