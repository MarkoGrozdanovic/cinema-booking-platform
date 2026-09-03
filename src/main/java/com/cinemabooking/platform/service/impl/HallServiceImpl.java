package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.exceptions.BusinessException;
import com.cinemabooking.platform.exceptions.ResourceNotFoundException;
import com.cinemabooking.platform.model.Cinema;
import com.cinemabooking.platform.model.Hall;
import com.cinemabooking.platform.model.Seat;
import com.cinemabooking.platform.model.request.CreateHallRequestDTO;
import com.cinemabooking.platform.model.request.CreateSeatRowRequestDTO;
import com.cinemabooking.platform.model.response.HallResponseDTO;
import com.cinemabooking.platform.repositories.CinemaRepository;
import com.cinemabooking.platform.repositories.HallRepository;
import com.cinemabooking.platform.service.HallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HallServiceImpl implements HallService {

    private final HallRepository hallRepository;
    private final CinemaRepository cinemaRepository;

    @Override
    @Transactional
    public HallResponseDTO createHall(
            CreateHallRequestDTO request
    ) {
        Cinema cinema = cinemaRepository
                .findById(request.getCinemaId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cinema with ID "
                                        + request.getCinemaId()
                                        + " was not found"
                        )
                );

        if (!cinema.isActive()) {
            throw new BusinessException(
                    "A hall cannot be added to an inactive cinema"
            );
        }

        String hallName = request.getName().trim();

        if (hallRepository
                .existsByNameIgnoreCaseAndCinemaId(
                        hallName,
                        cinema.getId()
                )) {
            throw new BusinessException(
                    "A hall with this name already exists in this cinema"
            );
        }

        validateUniqueRowLabels(request.getRows());

        Hall hall = new Hall();
        hall.setName(hallName);
        hall.setHallType(request.getHallType());
        hall.setCinema(cinema);
        hall.setActive(true);

        for (CreateSeatRowRequestDTO row : request.getRows()) {
            String rowLabel = row.getRowLabel()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            for (
                    int seatNumber = 1;
                    seatNumber <= row.getNumberOfSeats();
                    seatNumber++
            ) {
                Seat seat = new Seat();
                seat.setRowLabel(rowLabel);
                seat.setSeatNumber(seatNumber);
                seat.setSeatType(row.getSeatType());
                seat.setActive(true);

                hall.addSeat(seat);
            }
        }

        Hall savedHall = hallRepository.save(hall);

        return toHallResponse(savedHall);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HallResponseDTO> getAllHalls() {
        return hallRepository
                .findAllWithCinemaAndSeats()
                .stream()
                .map(this::toHallResponse)
                .toList();
    }

    private void validateUniqueRowLabels(
            List<CreateSeatRowRequestDTO> rows
    ) {
        Set<String> rowLabels = new HashSet<>();

        for (CreateSeatRowRequestDTO row : rows) {
            String normalizedLabel = row.getRowLabel()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (!rowLabels.add(normalizedLabel)) {
                throw new BusinessException(
                        "Duplicate seat row labels are not allowed"
                );
            }
        }
    }

    private HallResponseDTO toHallResponse(Hall hall) {
        return HallResponseDTO.builder()
                .id(hall.getId())
                .name(hall.getName())
                .hallType(hall.getHallType())
                .cinemaId(hall.getCinema().getId())
                .cinemaName(hall.getCinema().getName())
                .active(hall.isActive())
                .numberOfSeats(hall.getSeats().size())
                .build();
    }
}