package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.exceptions.BusinessException;
import com.cinemabooking.platform.model.Cinema;
import com.cinemabooking.platform.model.request.CreateCinemaRequestDTO;
import com.cinemabooking.platform.model.response.CinemaResponseDTO;
import com.cinemabooking.platform.repositories.CinemaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CinemaServiceImplTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @InjectMocks
    private CinemaServiceImpl cinemaService;

    @Test
    void createCinema_shouldNormalizeSaveAndReturnCinema() {
        CreateCinemaRequestDTO request =
                new CreateCinemaRequestDTO(
                        "  CineStar Novi Sad  ",
                        "  Bulevar oslobođenja 119  ",
                        "  Novi Sad  ",
                        "  Modern multiplex cinema  "
                );

        when(cinemaRepository
                .existsByNameIgnoreCaseAndCityIgnoreCase(
                        "CineStar Novi Sad",
                        "Novi Sad"
                ))
                .thenReturn(false);

        when(cinemaRepository.save(any(Cinema.class)))
                .thenAnswer(invocation -> {
                    Cinema cinema = invocation.getArgument(0);
                    cinema.setId(10L);
                    return cinema;
                });

        CinemaResponseDTO response =
                cinemaService.createCinema(request);

        ArgumentCaptor<Cinema> captor =
                ArgumentCaptor.forClass(Cinema.class);

        verify(cinemaRepository).save(captor.capture());

        Cinema savedCinema = captor.getValue();

        assertAll(
                () -> assertEquals(
                        "CineStar Novi Sad",
                        savedCinema.getName()
                ),
                () -> assertEquals(
                        "Bulevar oslobođenja 119",
                        savedCinema.getAddress()
                ),
                () -> assertEquals(
                        "Novi Sad",
                        savedCinema.getCity()
                ),
                () -> assertEquals(
                        "Modern multiplex cinema",
                        savedCinema.getDescription()
                ),
                () -> assertTrue(savedCinema.isActive()),
                () -> assertEquals(10L, response.getId()),
                () -> assertEquals(
                        "CineStar Novi Sad",
                        response.getName()
                ),
                () -> assertTrue(response.isActive())
        );
    }

    @Test
    void createCinema_shouldRejectDuplicateNameAndCity() {
        CreateCinemaRequestDTO request =
                new CreateCinemaRequestDTO(
                        "CineStar Novi Sad",
                        "Another address",
                        "Novi Sad",
                        null
                );

        when(cinemaRepository
                .existsByNameIgnoreCaseAndCityIgnoreCase(
                        "CineStar Novi Sad",
                        "Novi Sad"
                ))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cinemaService.createCinema(request)
        );

        assertEquals(
                "A cinema with this name already exists in this city",
                exception.getMessage()
        );

        verify(cinemaRepository, never())
                .save(any(Cinema.class));
    }

    @Test
    void createCinema_shouldConvertBlankDescriptionToNull() {
        CreateCinemaRequestDTO request =
                new CreateCinemaRequestDTO(
                        "Cinema",
                        "Address",
                        "Belgrade",
                        "   "
                );

        when(cinemaRepository
                .existsByNameIgnoreCaseAndCityIgnoreCase(
                        "Cinema",
                        "Belgrade"
                ))
                .thenReturn(false);

        when(cinemaRepository.save(any(Cinema.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        CinemaResponseDTO response =
                cinemaService.createCinema(request);

        assertNull(response.getDescription());
    }

    @Test
    void getAllCinemas_shouldReturnMappedCinemas() {
        Cinema cinema = new Cinema();
        cinema.setId(10L);
        cinema.setName("CineStar Novi Sad");
        cinema.setAddress("Bulevar oslobođenja 119");
        cinema.setCity("Novi Sad");
        cinema.setDescription("Description");
        cinema.setActive(true);

        when(cinemaRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(cinema));

        List<CinemaResponseDTO> response =
                cinemaService.getAllCinemas();

        assertEquals(1, response.size());

        CinemaResponseDTO result = response.get(0);

        assertAll(
                () -> assertEquals(10L, result.getId()),
                () -> assertEquals(
                        "CineStar Novi Sad",
                        result.getName()
                ),
                () -> assertEquals(
                        "Bulevar oslobođenja 119",
                        result.getAddress()
                ),
                () -> assertEquals(
                        "Novi Sad",
                        result.getCity()
                ),
                () -> assertTrue(result.isActive())
        );

        verify(cinemaRepository).findAll(any(Sort.class));
    }
}