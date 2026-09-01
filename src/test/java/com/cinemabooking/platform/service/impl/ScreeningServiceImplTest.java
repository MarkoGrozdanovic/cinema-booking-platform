package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.config.AppConstants;
import com.cinemabooking.platform.exceptions.*;
import com.cinemabooking.platform.model.*;
import com.cinemabooking.platform.model.enums.ScreeningSeatStatus;
import com.cinemabooking.platform.model.enums.ScreeningStatus;
import com.cinemabooking.platform.model.enums.SeatType;
import com.cinemabooking.platform.model.request.CreateScreeningRequestDTO;
import com.cinemabooking.platform.model.response.ScreeningResponseDTO;
import com.cinemabooking.platform.model.response.ScreeningSeatResponseDTO;
import com.cinemabooking.platform.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Mock
    private ScreeningSeatRepository screeningSeatRepository;

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

    @Test
    void getScreeningSeats_shouldReturnSeatMap() {
        Seat seat = new Seat();
        seat.setRowLabel("A");
        seat.setSeatNumber(1);
        seat.setSeatType(SeatType.VIP);

        ScreeningSeat screeningSeat = new ScreeningSeat();
        screeningSeat.setId(10L);
        screeningSeat.setSeat(seat);
        screeningSeat.setPrice(new BigDecimal("1200.00"));
        screeningSeat.setStatus(
                ScreeningSeatStatus.AVAILABLE
        );

        when(screeningRepository.existsById(2L))
                .thenReturn(true);

        when(screeningSeatRepository.findAllByScreeningId(2L))
                .thenReturn(List.of(screeningSeat));

        List<ScreeningSeatResponseDTO> response =
                screeningService.getScreeningSeats(2L);

        assertEquals(1, response.size());

        ScreeningSeatResponseDTO returnedSeat = response.get(0);

        assertAll(
                () -> assertEquals(
                        10L,
                        returnedSeat.getScreeningSeatId()
                ),
                () -> assertEquals(
                        "A",
                        returnedSeat.getRowLabel()
                ),
                () -> assertEquals(
                        1,
                        returnedSeat.getSeatNumber()
                ),
                () -> assertEquals(
                        SeatType.VIP,
                        returnedSeat.getSeatType()
                ),
                () -> assertEquals(
                        new BigDecimal("1200.00"),
                        returnedSeat.getPrice()
                ),
                () -> assertEquals(
                        ScreeningSeatStatus.AVAILABLE,
                        returnedSeat.getStatus()
                )
        );
    }

    @Test
    void getScreeningSeats_shouldThrowExceptionWhenScreeningDoesNotExist() {
        when(screeningRepository.existsById(999L))
                .thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> screeningService.getScreeningSeats(999L)
        );

        assertEquals(
                "Screening with ID 999 was not found",
                exception.getMessage()
        );

        verifyNoInteractions(screeningSeatRepository);
    }

    @Test
    void getUpcomingScreenings_shouldReturnMappedScheduledScreenings() {
        LocalDateTime startTime =
                LocalDateTime.of(2099, 9, 10, 19, 0);

        LocalDateTime endTime =
                LocalDateTime.of(2099, 9, 10, 21, 32);

        Movie movie = new Movie();
        movie.setId(10L);
        movie.setTitle("The Dark Knight");

        Cinema cinema = new Cinema();
        cinema.setName("Central Cinema");

        Hall hall = new Hall();
        hall.setId(20L);
        hall.setCinema(cinema);

        Screening screening = new Screening();
        screening.setId(30L);
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setStartTime(startTime);
        screening.setEndTime(endTime);
        screening.setBasePrice(
                new BigDecimal("800.00")
        );
        screening.setStatus(ScreeningStatus.SCHEDULED);

        screening.addScreeningSeat(new ScreeningSeat());
        screening.addScreeningSeat(new ScreeningSeat());

        when(screeningRepository.findUpcomingScreenings(
                any(LocalDateTime.class),
                eq(ScreeningStatus.SCHEDULED)
        )).thenReturn(List.of(screening));

        List<ScreeningResponseDTO> response =
                screeningService.getUpcomingScreenings();

        assertEquals(1, response.size());

        ScreeningResponseDTO result = response.get(0);

        assertAll(
                () -> assertEquals(30L, result.getId()),
                () -> assertEquals(10L, result.getMovieId()),
                () -> assertEquals(
                        "The Dark Knight",
                        result.getMovieTitle()
                ),
                () -> assertEquals(20L, result.getHallId()),
                () -> assertEquals(
                        "Central Cinema",
                        result.getCinemaName()
                ),
                () -> assertEquals(
                        startTime,
                        result.getStartTime()
                ),
                () -> assertEquals(
                        endTime,
                        result.getEndTime()
                ),
                () -> assertEquals(
                        new BigDecimal("800.00"),
                        result.getBasePrice()
                ),
                () -> assertEquals(
                        "SCHEDULED",
                        result.getStatus()
                ),
                () -> assertEquals(
                        2,
                        result.getNumberOfSeats()
                )
        );

        verify(screeningRepository)
                .findUpcomingScreenings(
                        any(LocalDateTime.class),
                        eq(ScreeningStatus.SCHEDULED)
                );
    }
}
