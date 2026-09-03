package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.exceptions.BusinessException;
import com.cinemabooking.platform.model.Cinema;
import com.cinemabooking.platform.model.request.CreateCinemaRequestDTO;
import com.cinemabooking.platform.model.response.CinemaResponseDTO;
import com.cinemabooking.platform.repositories.CinemaRepository;
import com.cinemabooking.platform.service.CinemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaServiceImpl implements CinemaService {

    private final CinemaRepository cinemaRepository;

    @Override
    @Transactional
    public CinemaResponseDTO createCinema(
            CreateCinemaRequestDTO request
    ) {
        String name = request.getName().trim();
        String city = request.getCity().trim();

        if (cinemaRepository
                .existsByNameIgnoreCaseAndCityIgnoreCase(
                        name,
                        city
                )) {
            throw new BusinessException(
                    "A cinema with this name already exists in this city"
            );
        }

        Cinema cinema = new Cinema();

        cinema.setName(name);
        cinema.setAddress(request.getAddress().trim());
        cinema.setCity(city);
        cinema.setDescription(
                normalizeOptional(request.getDescription())
        );
        cinema.setActive(true);

        Cinema savedCinema =
                cinemaRepository.save(cinema);

        return toCinemaResponse(savedCinema);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CinemaResponseDTO> getAllCinemas() {
        return cinemaRepository
                .findAll(
                        Sort.by(
                                Sort.Order.asc("city"),
                                Sort.Order.asc("name")
                        )
                )
                .stream()
                .map(this::toCinemaResponse)
                .toList();
    }

    private CinemaResponseDTO toCinemaResponse(
            Cinema cinema
    ) {
        return CinemaResponseDTO.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .address(cinema.getAddress())
                .city(cinema.getCity())
                .description(cinema.getDescription())
                .active(cinema.isActive())
                .build();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}