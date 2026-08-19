package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.config.AppConstants;
import com.cinemabooking.platform.exceptions.*;
import com.cinemabooking.platform.model.*;
import com.cinemabooking.platform.model.enums.AppRole;
import com.cinemabooking.platform.model.enums.BookingStatus;
import com.cinemabooking.platform.model.enums.ScreeningSeatStatus;
import com.cinemabooking.platform.model.enums.ScreeningStatus;
import com.cinemabooking.platform.model.request.CreateBookingRequestDTO;
import com.cinemabooking.platform.model.response.BookedSeatResponseDTO;
import com.cinemabooking.platform.model.response.BookingResponseDTO;
import com.cinemabooking.platform.repositories.BookingRepository;
import com.cinemabooking.platform.repositories.ScreeningRepository;
import com.cinemabooking.platform.repositories.ScreeningSeatRepository;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.service.BookingService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class BookingServiceImpl implements BookingService {

    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;
    private final ScreeningSeatRepository screeningSeatRepository;
    private final BookingRepository bookingRepository;

    public BookingServiceImpl(UserRepository userRepository, ScreeningRepository screeningRepository, ScreeningSeatRepository screeningSeatRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.screeningRepository = screeningRepository;
        this.screeningSeatRepository = screeningSeatRepository;
        this.bookingRepository = bookingRepository;
    }


    @Override
    @Transactional
    public BookingResponseDTO createBooking(CreateBookingRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();
        List<Long> screeningSeatIds = request.getScreeningSeatIds();
        Set<Long> uniqueScreeningSeatIds = new LinkedHashSet<>(screeningSeatIds);

        if (screeningSeatIds.size() != uniqueScreeningSeatIds.size()) {
            throw new BusinessException(
                    "Duplicate screening seat IDs are not allowed"
            );
        }

        AppUser user = userRepository
                .findByIdAndActiveTrueAndRole(
                        request.getUserId(),
                        AppRole.CUSTOMER
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active customer with ID " + request.getUserId() + " was not found"
                ));


        Screening screening = screeningRepository
                .findById(request.getScreeningId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Screening with ID " + request.getScreeningId()
                                + " was not found"
                ));

        if (screening.getStatus() != ScreeningStatus.SCHEDULED) {
            throw new InvalidScreeningException(
                    "Screening is not available for booking"
            );
        }

        if (!screening.getStartTime().isAfter(now)) {
            throw new InvalidScreeningException(
                    "Cannot book seats for a screening that has already started"
            );
        }

        List<ScreeningSeat> screeningSeats = screeningSeatRepository.findAllByIdsWithLock(uniqueScreeningSeatIds);

        if (screeningSeats.size() != uniqueScreeningSeatIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more selected screening seats do not exist"
            );
        }

        for(ScreeningSeat seat: screeningSeats){

            Long seatScreeningId = seat.getScreening().getId();

            if (!Objects.equals(seatScreeningId, request.getScreeningId())) {
                throw new InvalidSeatSelectionException(
                        "Seat with ID " + seat.getId()
                                + " does not belong to the requested screening"
                );
            }

            if (seat.getStatus() != ScreeningSeatStatus.AVAILABLE) {
                throw new SeatNotAvailableException(
                        "Seat with ID " + seat.getId()
                                + " is not available"
                );
            }
        }

        Booking booking = Booking.builder()
                .user(user)
                .screening(screening)
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(now.plusMinutes(AppConstants.BOOKING_HOLD_MINUTES))
                .build();

        for(ScreeningSeat seat: screeningSeats){
            seat.setStatus(ScreeningSeatStatus.HELD);
            seat.setReservedUntil(booking.getExpiresAt());
            BookingItem bookingItem = new BookingItem();
            bookingItem.setScreeningSeat(seat);
            bookingItem.setPrice(seat.getPrice());
            booking.addBookingItem(bookingItem);
        }

        BigDecimal total = BigDecimal.valueOf(0);

        for(ScreeningSeat seat: screeningSeats){
            total = total.add(seat.getPrice());
        }

        booking.setTotalPrice(total);

        Booking savedBooking = bookingRepository.save(booking);

        return toBookingResponse(savedBooking);
    }

    @Transactional
    @Override
    public void expirePendingBookings() {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> expiredBookings = bookingRepository
                .findExpirePendingBookings(
                        BookingStatus.PENDING_PAYMENT,
                        now
                );

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);

            for (BookingItem bookingItem : booking.getBookingItems()) {
                ScreeningSeat screeningSeat =
                        bookingItem.getScreeningSeat();

                if (screeningSeat.getStatus() ==
                        ScreeningSeatStatus.HELD) {

                    screeningSeat.setStatus(
                            ScreeningSeatStatus.AVAILABLE
                    );

                    screeningSeat.setReservedUntil(null);
                }
            }
        }
    }

    private BookingResponseDTO toBookingResponse(Booking booking){
        List<BookedSeatResponseDTO> selectedSeats =
                booking.getBookingItems()
                        .stream()
                        .map(item -> {
                            ScreeningSeat screeningSeat =
                                    item.getScreeningSeat();

                            Seat seat = screeningSeat.getSeat();

                            return new BookedSeatResponseDTO(
                                    screeningSeat.getId(),
                                    seat.getRowLabel(),
                                    seat.getSeatNumber(),
                                    seat.getSeatType().name(),
                                    item.getPrice()
                            );
                        })
                        .toList();

        Screening screening = booking.getScreening();

        return BookingResponseDTO.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .movieTitle(screening.getMovie().getTitle())
                .cinemaName(
                        screening.getHall()
                                .getCinema()
                                .getName()
                )
                .hallName(screening.getHall().getName())
                .screeningStartTime(screening.getStartTime())
                .expiresAt(booking.getExpiresAt())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .selectedSeats(selectedSeats)
                .build();
    }


}
