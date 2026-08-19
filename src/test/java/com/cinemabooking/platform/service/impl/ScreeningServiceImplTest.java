package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.config.AppConstants;
import com.cinemabooking.platform.exceptions.BusinessException;
import com.cinemabooking.platform.exceptions.HallHasNoActiveSeatsException;
import com.cinemabooking.platform.exceptions.InactiveMovieException;
import com.cinemabooking.platform.exceptions.ScreeningConflictException;
import com.cinemabooking.platform.model.*;
import com.cinemabooking.platform.model.enums.ScreeningSeatStatus;
import com.cinemabooking.platform.model.enums.ScreeningStatus;
import com.cinemabooking.platform.model.enums.SeatType;
import com.cinemabooking.platform.model.request.CreateScreeningRequestDTO;
import com.cinemabooking.platform.model.response.ScreeningResponseDTO;
import com.cinemabooking.platform.repositories.HallRepository;
import com.cinemabooking.platform.repositories.MovieRepository;
import com.cinemabooking.platform.repositories.ScreeningRepository;
import com.cinemabooking.platform.repositories.SeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ScreeningRepository screeningRepository;

    @InjectMocks
    private ScreeningServiceImpl screeningService;

    @Test
    void createScreening_shouldRejectRequest_whenMovieIsInactive() {
        // Arrange
        CreateScreeningRequestDTO request =
                new CreateScreeningRequestDTO(
                        1L,
                        2L,
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("750.00")
                );

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setActive(false);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(movie));

        // Act
        InactiveMovieException exception = assertThrows(
                InactiveMovieException.class,
                () -> screeningService.createScreening(request)
        );

        // Assert
        assertEquals(
                "An inactive movie cannot be assigned to a new screening",
                exception.getMessage()
        );

        verifyNoInteractions(
                hallRepository,
                seatRepository,
                screeningRepository
        );
    }

    @Test
    void createScreening_shouldRejectRequest_whenHallIsInactive() {
        // Arrange
        CreateScreeningRequestDTO request =
                new CreateScreeningRequestDTO(
                        1L,
                        2L,
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("750.00")
                );

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setActive(true);

        Hall hall = new Hall();
        hall.setId(2L);
        hall.setActive(false);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(movie));

        when(hallRepository.findByIdWithLock(2L))
                .thenReturn(Optional.of(hall));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> screeningService.createScreening(request)

        );

        assertEquals(
                "An inactive hall cannot host a screening",
                exception.getMessage()
                );

        verifyNoInteractions(
                seatRepository,
                screeningRepository
        );
    }

    @Test
    void createScreening_shouldRejectRequest_whenCinemaIsInactive(){
        CreateScreeningRequestDTO request =
                new CreateScreeningRequestDTO(
                        1L,
                        2L,
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("750.00")
                );

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setActive(true);

        Hall hall = new Hall();
        hall.setId(2L);
        hall.setActive(true);

        Cinema cinema = new Cinema();
        cinema.setId(1L);
        cinema.setActive(false);

        hall.setCinema(cinema);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(movie));

        when(hallRepository.findByIdWithLock(2L))
                .thenReturn(Optional.of(hall));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> screeningService.createScreening(request)

        );

        assertEquals(
                "A hall in an inactive cinema cannot host a screening",
                exception.getMessage()
        );

        verifyNoInteractions(
                seatRepository,
                screeningRepository
        );
    }

    @Test
    void createScreening_shouldRejectRequest_whenScreeningOverlaps() {
        // Arrange
        LocalDateTime startTime =
                LocalDateTime.now().plusDays(1);

        CreateScreeningRequestDTO request =
                new CreateScreeningRequestDTO(
                        1L,
                        2L,
                        startTime,
                        new BigDecimal("750.00")
                );

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setActive(true);
        movie.setDurationMinutes(120);

        Cinema cinema = new Cinema();
        cinema.setActive(true);

        Hall hall = new Hall();
        hall.setId(2L);
        hall.setActive(true);
        hall.setCinema(cinema);

        LocalDateTime hallAvailableAt = startTime
                .plusMinutes(120)
                .plusMinutes(AppConstants.CLEANING_HALL_TIME);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(movie));

        when(hallRepository.findByIdWithLock(2L))
                .thenReturn(Optional.of(hall));

        when(screeningRepository.existsConflictingScreening(
                hallAvailableAt,
                startTime,
                2L,
                ScreeningStatus.CANCELLED
        )).thenReturn(true);

        // Act
        ScreeningConflictException exception = assertThrows(
                ScreeningConflictException.class,
                () -> screeningService.createScreening(request)
        );

        // Assert
        assertEquals(
                "Hall 2 already has a screening during the selected time",
                exception.getMessage()
        );

        verifyNoInteractions(seatRepository);

        verify(screeningRepository, never())
                .save(any(Screening.class));
    }

    @Test
    void createScreening_shouldRejectRequest_whenHallHasNoActiveSeats() {
        // Arrange
        LocalDateTime startTime =
                LocalDateTime.now().plusDays(1);

        CreateScreeningRequestDTO request =
                new CreateScreeningRequestDTO(
                        1L,
                        2L,
                        startTime,
                        new BigDecimal("750.00")
                );

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setActive(true);
        movie.setDurationMinutes(120);

        Cinema cinema = new Cinema();
        cinema.setActive(true);

        Hall hall = new Hall();
        hall.setId(2L);
        hall.setActive(true);
        hall.setCinema(cinema);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(movie));

        when(hallRepository.findByIdWithLock(2L))
                .thenReturn(Optional.of(hall));

        when(screeningRepository.existsConflictingScreening(
                any(LocalDateTime.class),
                eq(startTime),
                eq(2L),
                eq(ScreeningStatus.CANCELLED)
        )).thenReturn(false);

        when(seatRepository.findAllByHallIdAndActiveTrue(2L))
                .thenReturn(List.of());

        // Act
        HallHasNoActiveSeatsException exception = assertThrows(
                HallHasNoActiveSeatsException.class,
                () -> screeningService.createScreening(request)
        );

        // Assert
        assertEquals(
                "Cannot create screening because the hall has no active seats",
                exception.getMessage()
        );

        verify(screeningRepository, never())
                .save(any(Screening.class));
    }

    @Test
    void createScreening_shouldCreateScreeningAndGenerateSeats() {
        // Arrange
        LocalDateTime startTime =
                LocalDateTime.now().plusDays(1);

        CreateScreeningRequestDTO request =
                new CreateScreeningRequestDTO(
                        1L,
                        2L,
                        startTime,
                        new BigDecimal("750.00")
                );

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Interstellar");
        movie.setActive(true);
        movie.setDurationMinutes(120);

        Cinema cinema = new Cinema();
        cinema.setName("Test Cinema");
        cinema.setActive(true);

        Hall hall = new Hall();
        hall.setId(2L);
        hall.setName("Hall 1");
        hall.setActive(true);
        hall.setCinema(cinema);

        Seat standardSeat = new Seat();
        standardSeat.setSeatType(SeatType.STANDARD);

        Seat vipSeat = new Seat();
        vipSeat.setSeatType(SeatType.VIP);

        Seat coupleSeat = new Seat();
        coupleSeat.setSeatType(SeatType.COUPLE);

        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(movie));

        when(hallRepository.findByIdWithLock(2L))
                .thenReturn(Optional.of(hall));

        when(screeningRepository.existsConflictingScreening(
                any(LocalDateTime.class),
                eq(startTime),
                eq(2L),
                eq(ScreeningStatus.CANCELLED)
        )).thenReturn(false);

        when(seatRepository.findAllByHallIdAndActiveTrue(2L))
                .thenReturn(List.of(
                        standardSeat,
                        vipSeat,
                        coupleSeat
                ));

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> {
                    Screening screening = invocation.getArgument(0);
                    screening.setId(100L);
                    return screening;
                });

        // Act
        ScreeningResponseDTO response =
                screeningService.createScreening(request);

        // Assert
        assertEquals(100L, response.getId());
        assertEquals("Interstellar", response.getMovieTitle());
        assertEquals("Test Cinema", response.getCinemaName());
        assertEquals(3, response.getNumberOfSeats());
        assertEquals(startTime.plusMinutes(120), response.getEndTime());

        ArgumentCaptor<Screening> captor =
                ArgumentCaptor.forClass(Screening.class);

        verify(screeningRepository).save(captor.capture());

        Screening savedScreening = captor.getValue();

        assertEquals(3, savedScreening.getScreeningSeats().size());

        assertEquals(
                0,
                savedScreening.getScreeningSeats()
                        .get(0)
                        .getPrice()
                        .compareTo(new BigDecimal("750.00"))
        );

        assertEquals(
                0,
                savedScreening.getScreeningSeats()
                        .get(1)
                        .getPrice()
                        .compareTo(new BigDecimal("1125.00"))
        );

        assertEquals(
                0,
                savedScreening.getScreeningSeats()
                        .get(2)
                        .getPrice()
                        .compareTo(new BigDecimal("1500.00"))
        );

        assertTrue(
                savedScreening.getScreeningSeats()
                        .stream()
                        .allMatch(seat ->
                                seat.getStatus()
                                        == ScreeningSeatStatus.AVAILABLE
                        )
        );
    }
}
