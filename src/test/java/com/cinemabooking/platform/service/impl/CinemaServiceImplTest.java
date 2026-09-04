package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.exceptions.BusinessException;
import com.cinemabooking.platform.model.Cinema;
import com.cinemabooking.platform.model.enums.ScreeningStatus;
import com.cinemabooking.platform.model.request.CreateCinemaRequestDTO;
import com.cinemabooking.platform.model.response.CinemaResponseDTO;
import com.cinemabooking.platform.repositories.CinemaRepository;
import com.cinemabooking.platform.repositories.ScreeningRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaServiceImplTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @InjectMocks
    private CinemaServiceImpl cinemaService;

    @Mock
    private ScreeningRepository screeningRepository;

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

    @Test
    void updateCinemaStatus_shouldDeactivateCinemaWithoutFutureScreenings() {
        Cinema cinema = new Cinema();
        cinema.setId(10L);
        cinema.setName("Central Cinema");
        cinema.setActive(true);

        when(cinemaRepository.findById(10L))
                .thenReturn(Optional.of(cinema));

        when(screeningRepository
                .existsFutureScreeningForCinema(
                        eq(10L),
                        eq(ScreeningStatus.SCHEDULED),
                        any(LocalDateTime.class)
                ))
                .thenReturn(false);

        CinemaResponseDTO response =
                cinemaService.updateCinemaStatus(
                        10L,
                        false
                );

        assertFalse(cinema.isActive());
        assertFalse(response.isActive());
    }

    @Test
    void updateCinemaStatus_shouldRejectDeactivationWithFutureScreenings() {
        Cinema cinema = new Cinema();
        cinema.setId(10L);
        cinema.setName("Central Cinema");
        cinema.setActive(true);

        when(cinemaRepository.findById(10L))
                .thenReturn(Optional.of(cinema));

        when(screeningRepository
                .existsFutureScreeningForCinema(
                        eq(10L),
                        eq(ScreeningStatus.SCHEDULED),
                        any(LocalDateTime.class)
                ))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cinemaService.updateCinemaStatus(
                        10L,
                        false
                )
        );

        assertEquals(
                "Cinema cannot be deactivated while it has future scheduled screenings",
                exception.getMessage()
        );

        assertTrue(cinema.isActive());
    }

    @Test
    void updateCinemaStatus_shouldActivateCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(10L);
        cinema.setName("Central Cinema");
        cinema.setActive(false);

        when(cinemaRepository.findById(10L))
                .thenReturn(Optional.of(cinema));

        CinemaResponseDTO response =
                cinemaService.updateCinemaStatus(
                        10L,
                        true
                );

        assertTrue(cinema.isActive());
        assertTrue(response.isActive());

        verifyNoInteractions(screeningRepository);
    }
}