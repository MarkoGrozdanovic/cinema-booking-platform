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
import org.springframework.transaction.annotation.Transactional;
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
    public BookingResponseDTO createBooking(CreateBookingRequestDTO request, Long id) {
        LocalDateTime now = LocalDateTime.now();

        Set<Long> uniqueScreeningSeatIds = validateAndGetUniqueSeatIds(request.getScreeningSeatIds());

        AppUser user = findActiveCustomer(id);


        Screening screening = findAndValidateScreening(
                request.getScreeningId(),
                now
        );

        List<ScreeningSeat> screeningSeats =
                findAndValidateScreeningSeats(
                        uniqueScreeningSeatIds,
                        screening.getId()
                );

        Booking booking = createPendingBooking(
                user,
                screening,
                now
        );

        holdSeatsAndCreateBookingItems(
                booking,
                screeningSeats
        );

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

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookings(Long userId) {
        return bookingRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toBookingResponse)
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking with ID " + bookingId + " was not found"
                ));
        return toBookingResponse(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking with ID " + bookingId + " was not found"
                ));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException(
                    "Only pending payment bookings can be cancelled"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        for (BookingItem bookingItem : booking.getBookingItems()) {
            ScreeningSeat screeningSeat =
                    bookingItem.getScreeningSeat();

            if (screeningSeat.getStatus()
                    == ScreeningSeatStatus.HELD) {

                screeningSeat.setStatus(
                        ScreeningSeatStatus.AVAILABLE
                );
                screeningSeat.setReservedUntil(null);
            }
        }

    }

    private Set<Long> validateAndGetUniqueSeatIds(
            List<Long> screeningSeatIds
    ) {
        Set<Long> uniqueSeatIds =
                new LinkedHashSet<>(screeningSeatIds);

        if (screeningSeatIds.size() != uniqueSeatIds.size()) {
            throw new BusinessException(
                    "Duplicate screening seat IDs are not allowed"
            );
        }

        return uniqueSeatIds;
    }

    private AppUser findActiveCustomer(Long userId) {
        return userRepository
                .findByIdAndActiveTrueAndRole(
                        userId,
                        AppRole.CUSTOMER
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active customer with ID "
                                + userId
                                + " was not found"
                ));
    }

    private Screening findAndValidateScreening(
            Long screeningId,
            LocalDateTime now
    ) {
        Screening screening = screeningRepository
                .findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Screening with ID "
                                + screeningId
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

        return screening;
    }

    private List<ScreeningSeat> findAndValidateScreeningSeats(
            Set<Long> seatIds,
            Long screeningId
    ) {
        List<ScreeningSeat> screeningSeats =
                screeningSeatRepository.findAllByIdsWithLock(seatIds);

        if (screeningSeats.size() != seatIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more selected screening seats do not exist"
            );
        }

        for (ScreeningSeat screeningSeat : screeningSeats) {
            validateScreeningSeat(screeningSeat, screeningId);
        }

        return screeningSeats;
    }

    private void validateScreeningSeat(
            ScreeningSeat screeningSeat,
            Long screeningId
    ) {
        Long seatScreeningId =
                screeningSeat.getScreening().getId();

        if (!Objects.equals(seatScreeningId, screeningId)) {
            throw new InvalidSeatSelectionException(
                    "Seat with ID "
                            + screeningSeat.getId()
                            + " does not belong to the requested screening"
            );
        }

        if (screeningSeat.getStatus()
                != ScreeningSeatStatus.AVAILABLE) {
            throw new SeatNotAvailableException(
                    "Seat with ID "
                            + screeningSeat.getId()
                            + " is not available"
            );
        }
    }

    private Booking createPendingBooking(
            AppUser user,
            Screening screening,
            LocalDateTime now
    ) {
        return Booking.builder()
                .user(user)
                .screening(screening)
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(
                        now.plusMinutes(
                                AppConstants.BOOKING_HOLD_MINUTES
                        )
                )
                .build();
    }

    private void holdSeatsAndCreateBookingItems(
            Booking booking,
            List<ScreeningSeat> screeningSeats
    ) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (ScreeningSeat screeningSeat : screeningSeats) {
            screeningSeat.setStatus(ScreeningSeatStatus.HELD);
            screeningSeat.setReservedUntil(booking.getExpiresAt());

            BookingItem bookingItem = new BookingItem();
            bookingItem.setScreeningSeat(screeningSeat);
            bookingItem.setPrice(screeningSeat.getPrice());

            booking.addBookingItem(bookingItem);

            totalPrice = totalPrice.add(screeningSeat.getPrice());
        }

        booking.setTotalPrice(totalPrice);
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
