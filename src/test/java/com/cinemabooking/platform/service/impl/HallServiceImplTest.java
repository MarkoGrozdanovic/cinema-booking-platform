package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.exceptions.BusinessException;
import com.cinemabooking.platform.exceptions.ResourceNotFoundException;
import com.cinemabooking.platform.model.Cinema;
import com.cinemabooking.platform.model.Hall;
import com.cinemabooking.platform.model.Seat;
import com.cinemabooking.platform.model.enums.HallType;
import com.cinemabooking.platform.model.enums.SeatType;
import com.cinemabooking.platform.model.request.CreateHallRequestDTO;
import com.cinemabooking.platform.model.request.CreateSeatRowRequestDTO;
import com.cinemabooking.platform.model.response.HallResponseDTO;
import com.cinemabooking.platform.repositories.CinemaRepository;
import com.cinemabooking.platform.repositories.HallRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HallServiceImplTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @InjectMocks
    private HallServiceImpl hallService;

    @Test
    void createHall_shouldGenerateNormalizeAndSaveSeats() {
        Cinema cinema = activeCinema();

        CreateHallRequestDTO request =
                new CreateHallRequestDTO(
                        1L,
                        "  Hall 2  ",
                        HallType.STANDARD,
                        List.of(
                                new CreateSeatRowRequestDTO(
                                        "a",
                                        2,
                                        SeatType.STANDARD
                                ),
                                new CreateSeatRowRequestDTO(
                                        " b ",
                                        1,
                                        SeatType.VIP
                                )
                        )
                );

        when(cinemaRepository.findById(1L))
                .thenReturn(Optional.of(cinema));

        when(hallRepository
                .existsByNameIgnoreCaseAndCinemaId(
                        "Hall 2",
                        1L
                ))
                .thenReturn(false);

        when(hallRepository.save(any(Hall.class)))
                .thenAnswer(invocation -> {
                    Hall hall = invocation.getArgument(0);
                    hall.setId(10L);
                    return hall;
                });

        HallResponseDTO response =
                hallService.createHall(request);

        ArgumentCaptor<Hall> captor =
                ArgumentCaptor.forClass(Hall.class);

        verify(hallRepository).save(captor.capture());

        Hall savedHall = captor.getValue();
        List<Seat> seats = savedHall.getSeats();

        assertAll(
                () -> assertEquals(
                        "Hall 2",
                        savedHall.getName()
                ),
                () -> assertEquals(
                        HallType.STANDARD,
                        savedHall.getHallType()
                ),
                () -> assertEquals(
                        cinema,
                        savedHall.getCinema()
                ),
                () -> assertTrue(savedHall.isActive()),
                () -> assertEquals(3, seats.size())
        );

        assertAll(
                () -> assertEquals(
                        "A",
                        seats.get(0).getRowLabel()
                ),
                () -> assertEquals(
                        1,
                        seats.get(0).getSeatNumber()
                ),
                () -> assertEquals(
                        SeatType.STANDARD,
                        seats.get(0).getSeatType()
                ),
                () -> assertEquals(
                        "A",
                        seats.get(1).getRowLabel()
                ),
                () -> assertEquals(
                        2,
                        seats.get(1).getSeatNumber()
                ),
                () -> assertEquals(
                        "B",
                        seats.get(2).getRowLabel()
                ),
                () -> assertEquals(
                        SeatType.VIP,
                        seats.get(2).getSeatType()
                ),
                () -> assertTrue(
                        seats.stream()
                                .allMatch(seat ->
                                        seat.getHall()
                                                == savedHall
                                )
                )
        );

        assertAll(
                () -> assertEquals(10L, response.getId()),
                () -> assertEquals(
                        "Hall 2",
                        response.getName()
                ),
                () -> assertEquals(
                        "Central Cinema",
                        response.getCinemaName()
                ),
                () -> assertEquals(
                        3,
                        response.getNumberOfSeats()
                ),
                () -> assertTrue(response.isActive())
        );
    }

    @Test
    void createHall_shouldThrowWhenCinemaDoesNotExist() {
        when(cinemaRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> hallService.createHall(
                        validRequest(999L)
                )
        );

        assertEquals(
                "Cinema with ID 999 was not found",
                exception.getMessage()
        );

        verifyNoInteractions(hallRepository);
    }

    @Test
    void createHall_shouldRejectInactiveCinema() {
        Cinema cinema = activeCinema();
        cinema.setActive(false);

        when(cinemaRepository.findById(1L))
                .thenReturn(Optional.of(cinema));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> hallService.createHall(
                        validRequest(1L)
                )
        );

        assertEquals(
                "A hall cannot be added to an inactive cinema",
                exception.getMessage()
        );

        verifyNoInteractions(hallRepository);
    }

    @Test
    void createHall_shouldRejectDuplicateHallName() {
        Cinema cinema = activeCinema();

        when(cinemaRepository.findById(1L))
                .thenReturn(Optional.of(cinema));

        when(hallRepository
                .existsByNameIgnoreCaseAndCinemaId(
                        "Hall 2",
                        1L
                ))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> hallService.createHall(
                        validRequest(1L)
                )
        );

        assertEquals(
                "A hall with this name already exists in this cinema",
                exception.getMessage()
        );

        verify(hallRepository, never())
                .save(any(Hall.class));
    }

    @Test
    void createHall_shouldRejectDuplicateRowLabels() {
        Cinema cinema = activeCinema();

        CreateHallRequestDTO request =
                new CreateHallRequestDTO(
                        1L,
                        "Hall 2",
                        HallType.STANDARD,
                        List.of(
                                new CreateSeatRowRequestDTO(
                                        "A",
                                        10,
                                        SeatType.STANDARD
                                ),
                                new CreateSeatRowRequestDTO(
                                        " a ",
                                        8,
                                        SeatType.VIP
                                )
                        )
                );

        when(cinemaRepository.findById(1L))
                .thenReturn(Optional.of(cinema));

        when(hallRepository
                .existsByNameIgnoreCaseAndCinemaId(
                        "Hall 2",
                        1L
                ))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> hallService.createHall(request)
        );

        assertEquals(
                "Duplicate seat row labels are not allowed",
                exception.getMessage()
        );

        verify(hallRepository, never())
                .save(any(Hall.class));
    }

    @Test
    void getAllHalls_shouldReturnMappedHalls() {
        Cinema cinema = activeCinema();

        Hall hall = new Hall();
        hall.setId(10L);
        hall.setName("Hall 2");
        hall.setHallType(HallType.IMAX);
        hall.setCinema(cinema);
        hall.setActive(true);

        hall.addSeat(seat("A", 1));
        hall.addSeat(seat("A", 2));

        when(hallRepository.findAllWithCinemaAndSeats())
                .thenReturn(List.of(hall));

        List<HallResponseDTO> response =
                hallService.getAllHalls();

        assertEquals(1, response.size());

        HallResponseDTO result = response.get(0);

        assertAll(
                () -> assertEquals(10L, result.getId()),
                () -> assertEquals(
                        "Hall 2",
                        result.getName()
                ),
                () -> assertEquals(
                        HallType.IMAX,
                        result.getHallType()
                ),
                () -> assertEquals(
                        1L,
                        result.getCinemaId()
                ),
                () -> assertEquals(
                        "Central Cinema",
                        result.getCinemaName()
                ),
                () -> assertEquals(
                        2,
                        result.getNumberOfSeats()
                )
        );

        verify(hallRepository)
                .findAllWithCinemaAndSeats();
    }

    private Cinema activeCinema() {
        Cinema cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Central Cinema");
        cinema.setActive(true);
        return cinema;
    }

    private CreateHallRequestDTO validRequest(
            Long cinemaId
    ) {
        return new CreateHallRequestDTO(
                cinemaId,
                "Hall 2",
                HallType.STANDARD,
                List.of(
                        new CreateSeatRowRequestDTO(
                                "A",
                                10,
                                SeatType.STANDARD
                        )
                )
        );
    }

    private Seat seat(
            String rowLabel,
            int seatNumber
    ) {
        Seat seat = new Seat();
        seat.setRowLabel(rowLabel);
        seat.setSeatNumber(seatNumber);
        seat.setSeatType(SeatType.STANDARD);
        seat.setActive(true);
        return seat;
    }
}