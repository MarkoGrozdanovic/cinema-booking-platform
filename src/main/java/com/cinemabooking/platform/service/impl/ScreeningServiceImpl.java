package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.config.AppConstants;
import com.cinemabooking.platform.exceptions.*;
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
import com.cinemabooking.platform.service.ScreeningSerivce;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScreeningServiceImpl implements ScreeningSerivce {

    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final ScreeningRepository screeningRepository;

    public ScreeningServiceImpl(MovieRepository movieRepository, HallRepository hallRepository, SeatRepository seatRepository, ScreeningRepository screeningRepository) {
        this.movieRepository = movieRepository;
        this.hallRepository = hallRepository;
        this.seatRepository = seatRepository;
        this.screeningRepository = screeningRepository;
    }

    @Override
    @Transactional
    public ScreeningResponseDTO createScreening(CreateScreeningRequestDTO request) {
        Movie movie = findMovie(request.getMovieId());
        Hall hall = findHall(request.getHallId());

        LocalDateTime movieEndTime = request.getStartTime()
                .plusMinutes(movie.getDurationMinutes());

        LocalDateTime hallAvailableAt = movieEndTime
                .plusMinutes(AppConstants.CLEANING_HALL_TIME);

        boolean conflictScreening = screeningRepository.existsConflictingScreening(hallAvailableAt, request.getStartTime(), request.getHallId(), ScreeningStatus.CANCELLED);
        if(conflictScreening){
            throw new ScreeningConflictException("Hall " + request.getHallId() + " already has a screening during the selected time");
        }

        List<Seat> activeSeats = seatRepository.findAllByHallIdAndActiveTrue(request.getHallId());

        if(activeSeats.isEmpty()){
            throw new HallHasNoActiveSeatsException("Cannot create screening because the hall has no active seats");
        }

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setBasePrice(request.getBasePrice());
        screening.setStartTime(request.getStartTime());
        screening.setEndTime(movieEndTime);
        screening.setHallAvailableAt(hallAvailableAt);


        for(Seat seat: activeSeats){
            ScreeningSeat screeningSeat = new ScreeningSeat();

            screeningSeat.setSeat(seat);
            screeningSeat.setStatus(ScreeningSeatStatus.AVAILABLE);
            screeningSeat.setPrice(calculateSeatPrice(request.getBasePrice(), seat));

            screening.addScreeningSeat(screeningSeat);
        }

        Screening savedScreening = screeningRepository.save(screening);

        return toScreeningResponse(savedScreening);

    }

    private ScreeningResponseDTO toScreeningResponse(Screening screening) {
        return ScreeningResponseDTO.builder()
                .id(screening.getId())
                .movieId(screening.getMovie().getId())
                .movieTitle(screening.getMovie().getTitle())
                .hallId(screening.getHall().getId())
                .cinemaName(screening.getHall().getCinema().getName())
                .startTime(screening.getStartTime())
                .endTime(screening.getEndTime())
                .basePrice(screening.getBasePrice())
                .status(screening.getStatus().name())
                .numberOfSeats(screening.getScreeningSeats().size())
                .build();
    }

    private BigDecimal calculateSeatPrice(BigDecimal basePrice, Seat seat){
        if (seat.getSeatType() == SeatType.VIP) {
            return basePrice.multiply(new BigDecimal("1.50"));
        } else if (seat.getSeatType() == SeatType.COUPLE) {
            return basePrice.multiply(new BigDecimal("2.00"));
        }

        return basePrice;
    }


    private Movie findMovie(Long movieId) throws BusinessException {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie with ID " + movieId + " was not found"));

        if(!movie.isActive()){
            throw new InactiveMovieException("An inactive movie cannot be assigned to a new screening");
        }
        return movie;
    }

    private Hall findHall(Long hallId){
        Hall hall =  hallRepository.findByIdWithLock(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Hall with ID " + hallId + " was not found"));

        if (!hall.isActive()) {
            throw new BusinessException(
                    "An inactive hall cannot host a screening"
            );
        }

        if (!hall.getCinema().isActive()) {
            throw new BusinessException(
                    "A hall in an inactive cinema cannot host a screening"
            );
        }

        return hall;
    }
}
