package com.cinemabooking.platform.service.impl;

import com.cinemabooking.platform.exceptions.*;
import com.cinemabooking.platform.model.*;
import com.cinemabooking.platform.model.enums.*;
import com.cinemabooking.platform.model.request.CreateBookingRequestDTO;
import com.cinemabooking.platform.model.response.BookingResponseDTO;
import com.cinemabooking.platform.repositories.BookingRepository;
import com.cinemabooking.platform.repositories.ScreeningRepository;
import com.cinemabooking.platform.repositories.ScreeningSeatRepository;
import com.cinemabooking.platform.repositories.UserRepository;
import com.cinemabooking.platform.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private ScreeningSeatRepository screeningSeatRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_shouldRejectDuplicateSeatIds() {
        CreateBookingRequestDTO request =
                new CreateBookingRequestDTO(1L, List.of(5L, 5L));

        Long authenticatedUserId = 1L;

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.createBooking(request, authenticatedUserId)
        );

        // Assert
        assertEquals(
                "Duplicate screening seat IDs are not allowed",
                exception.getMessage()
        );

        verifyNoInteractions(
                userRepository,
                screeningRepository,
                screeningSeatRepository,
                bookingRepository
        );
    }

    @Test
    void createBooking_shouldRejectRequest_whenActiveCustomerDoesNotExist() {
        CreateBookingRequestDTO requestDTO =
                new CreateBookingRequestDTO(1L, List.of(5L, 6L));

        Long authenticatedUserId = 1L;

        when(userRepository.findByIdAndActiveTrueAndRole(1L, AppRole.CUSTOMER))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(requestDTO, authenticatedUserId)
        );

        assertEquals("Active customer with ID 1 was not found",
                exception.getMessage());

        verify(userRepository).findByIdAndActiveTrueAndRole(
                1L,
                AppRole.CUSTOMER
        );

        verifyNoInteractions(
                screeningRepository,
                screeningSeatRepository,
                bookingRepository
        );
    }

    @Test
    void createBooking_shouldRejectRequest_whenScreeningDoesNotExist() {
        // Arrange
        CreateBookingRequestDTO request =
                new CreateBookingRequestDTO(
                        10L,
                        List.of(5L, 6L)
                );

        AppUser user = new AppUser();
        user.setId(1L);

        when(userRepository.findByIdAndActiveTrueAndRole(
                1L,
                AppRole.CUSTOMER
        )).thenReturn(Optional.of(user));

        when(screeningRepository.findById(10L))
                .thenReturn(Optional.empty());

        Long authenticatedUserId = 1L;

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(request, authenticatedUserId)
        );

        // Assert
        assertEquals(
                "Screening with ID 10 was not found",
                exception.getMessage()
        );

        verifyNoInteractions(
                screeningSeatRepository,
                bookingRepository
        );
    }

    @Test
    void createBooking_shouldRejectRequest_whenScreeningIsNotScheduled() {
        // Arrange
        CreateBookingRequestDTO request =
                new CreateBookingRequestDTO(
                        10L,
                        List.of(5L, 6L)
                );

        AppUser user = new AppUser();
        user.setId(1L);

        Screening screening = new Screening();
        screening.setId(10L);
        screening.setStatus(ScreeningStatus.CANCELLED);
        screening.setStartTime(LocalDateTime.now().plusDays(1));

        when(userRepository.findByIdAndActiveTrueAndRole(
                1L,
                AppRole.CUSTOMER
        )).thenReturn(Optional.of(user));

        when(screeningRepository.findById(10L))
                .thenReturn(Optional.of(screening));

        Long authenticatedUserId = 1L;

        // Act
        InvalidScreeningException exception = assertThrows(
                InvalidScreeningException.class,
                () -> bookingService.createBooking(request, authenticatedUserId)
        );

        // Assert
        assertEquals(
                "Screening is not available for booking",
                exception.getMessage()
        );

        verifyNoInteractions(
                screeningSeatRepository,
                bookingRepository
        );
    }

    @Test
    void createBooking_shouldRejectRequest_whenScreeningHasAlreadyStarted() {
        // Arrange
        CreateBookingRequestDTO request =
                new CreateBookingRequestDTO(
                        10L,
                        List.of(5L, 6L)
                );

        AppUser user = new AppUser();
        user.setId(1L);

        Screening screening = new Screening();
        screening.setId(10L);
        screening.setStatus(ScreeningStatus.SCHEDULED);
        screening.setStartTime(LocalDateTime.now().minusMinutes(10));

        when(userRepository.findByIdAndActiveTrueAndRole(
                1L,
                AppRole.CUSTOMER
        )).thenReturn(Optional.of(user));

        when(screeningRepository.findById(10L))
                .thenReturn(Optional.of(screening));

        Long authenticatedUserId = 1L;

        // Act
        InvalidScreeningException exception = assertThrows(
                InvalidScreeningException.class,
                () -> bookingService.createBooking(request, authenticatedUserId)
        );

        // Assert
        assertEquals(
                "Cannot book seats for a screening that has already started",
                exception.getMessage()
        );

        verifyNoInteractions(
                screeningSeatRepository,
                bookingRepository
        );
    }

    @Test
    void createBooking_shouldRejectRequest_whenSomeSeatsDoNotExist() {
        // Arrange
        CreateBookingRequestDTO request =
                new CreateBookingRequestDTO(
                        10L,
                        List.of(5L, 6L)
                );

        AppUser user = new AppUser();
        user.setId(1L);

        Screening screening = new Screening();
        screening.setId(10L);
        screening.setStatus(ScreeningStatus.SCHEDULED);
        screening.setStartTime(LocalDateTime.now().plusDays(1));

        ScreeningSeat existingSeat = new ScreeningSeat();
        existingSeat.setId(5L);

        when(userRepository.findByIdAndActiveTrueAndRole(
                1L,
                AppRole.CUSTOMER
        )).thenReturn(Optional.of(user));

        when(screeningRepository.findById(10L))
                .thenReturn(Optional.of(screening));

        when(screeningSeatRepository.findAllByIdsWithLock(
                Set.of(5L, 6L)
        )).thenReturn(List.of(existingSeat));

        Long authenticatedUserId = 1L;

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(request, authenticatedUserId)
        );

        // Assert
        assertEquals(
                "One or more selected screening seats do not exist",
                exception.getMessage()
        );

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_shouldRejectRequest_whenSeatBelongsToAnotherScreening() {
        // Arrange
        CreateBookingRequestDTO request =
                new CreateBookingRequestDTO(
                        10L,
                        List.of(5L)
                );

        AppUser user = new AppUser();
        user.setId(1L);

        Screening requestedScreening = new Screening();
        requestedScreening.setId(10L);
        requestedScreening.setStatus(ScreeningStatus.SCHEDULED);
        requestedScreening.setStartTime(
                LocalDateTime.now().plusDays(1)
        );

        Screening differentScreening = new Screening();
        differentScreening.setId(20L);

        ScreeningSeat screeningSeat = new ScreeningSeat();
        screeningSeat.setId(5L);
        screeningSeat.setScreening(differentScreening);
        screeningSeat.setStatus(ScreeningSeatStatus.AVAILABLE);

        when(userRepository.findByIdAndActiveTrueAndRole(
                1L,
                AppRole.CUSTOMER
        )).thenReturn(Optional.of(user));

        when(screeningRepository.findById(10L))
                .thenReturn(Optional.of(requestedScreening));

        when(screeningSeatRepository.findAllByIdsWithLock(
                Set.of(5L)
        )).thenReturn(List.of(screeningSeat));

        Long authenticatedUserId = 1L;

        // Act
        InvalidSeatSelectionException exception = assertThrows(
                InvalidSeatSelectionException.class,
                () -> bookingService.createBooking(request, authenticatedUserId)
        );

        // Assert
        assertEquals(
                "Seat with ID 5 does not belong to the requested screening",
                exception.getMessage()
        );

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_shouldRejectRequest_whenSeatIsNotAvailable() {
        CreateBookingRequestDTO request =
                new CreateBookingRequestDTO(
                        10L,
                        List.of(5L)
                );

        AppUser user = new AppUser();
        user.setId(1L);

        Screening screening = new Screening();
        screening.setId(10L);
        screening.setStatus(ScreeningStatus.SCHEDULED);
        screening.setStartTime(
                LocalDateTime.now().plusDays(1)
        );

        ScreeningSeat screeningSeat = new ScreeningSeat();
        screeningSeat.setId(5L);
        screeningSeat.setScreening(screening);
        screeningSeat.setStatus(ScreeningSeatStatus.HELD);

        when(userRepository.findByIdAndActiveTrueAndRole(
                1L,
                AppRole.CUSTOMER
        )).thenReturn(Optional.of(user));

        when(screeningRepository.findById(10L))
                .thenReturn(Optional.of(screening));

        when(screeningSeatRepository.findAllByIdsWithLock(
                Set.of(5L)
        )).thenReturn(List.of(screeningSeat));

        Long authenticatedUserId = 1L;

        SeatNotAvailableException exception = assertThrows(
                SeatNotAvailableException.class,
                () -> bookingService.createBooking(request, authenticatedUserId)
        );

        assertEquals(
                "Seat with ID 5 is not available",
                exception.getMessage()
        );

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_shouldCreateBooking_whenSeatsAreAvailable() {
        CreateBookingRequestDTO request =
                new CreateBookingRequestDTO(
                        10L,
                        List.of(5L, 6L)
                );

        AppUser user = new AppUser();
        user.setId(1L);

        Cinema cinema = new Cinema();
        cinema.setName("Test Cinema");

        Hall hall = new Hall();
        hall.setName("Hall 1");
        hall.setCinema(cinema);

        Movie movie = new Movie();
        movie.setTitle("Interstellar");

        Screening screening = new Screening();
        screening.setId(10L);
        screening.setStatus(ScreeningStatus.SCHEDULED);
        screening.setStartTime(
                LocalDateTime.now().plusDays(1)
        );
        screening.setHall(hall);
        screening.setMovie(movie);

        Seat physicalSeat1 = new Seat();
        physicalSeat1.setRowLabel("A");
        physicalSeat1.setSeatNumber(1);
        physicalSeat1.setSeatType(SeatType.STANDARD);

        Seat physicalSeat2 = new Seat();
        physicalSeat2.setRowLabel("A");
        physicalSeat2.setSeatNumber(2);
        physicalSeat2.setSeatType(SeatType.VIP);

        ScreeningSeat screeningSeat1 = new ScreeningSeat();
        screeningSeat1.setId(5L);
        screeningSeat1.setScreening(screening);
        screeningSeat1.setSeat(physicalSeat1);
        screeningSeat1.setStatus(ScreeningSeatStatus.AVAILABLE);
        screeningSeat1.setPrice(new BigDecimal("750.00"));

        ScreeningSeat screeningSeat2 = new ScreeningSeat();
        screeningSeat2.setId(6L);
        screeningSeat2.setScreening(screening);
        screeningSeat2.setSeat(physicalSeat2);
        screeningSeat2.setStatus(ScreeningSeatStatus.AVAILABLE);
        screeningSeat2.setPrice(new BigDecimal("900.00"));

        when(userRepository.findByIdAndActiveTrueAndRole(
                1L,
                AppRole.CUSTOMER
        )).thenReturn(Optional.of(user));

        when(screeningRepository.findById(10L))
                .thenReturn(Optional.of(screening));

        when(screeningSeatRepository.findAllByIdsWithLock(
                Set.of(5L, 6L)
        )).thenReturn(List.of(screeningSeat1, screeningSeat2));

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Long authenticatedUserId = 1L;

        BookingResponseDTO response =
                bookingService.createBooking(request, authenticatedUserId);

        assertEquals(
                BookingStatus.PENDING_PAYMENT,
                response.getStatus()
        );

        assertEquals(
                new BigDecimal("1650.00"),
                response.getTotalPrice()
        );

        assertEquals(2, response.getSelectedSeats().size());

        assertEquals(
                ScreeningSeatStatus.HELD,
                screeningSeat1.getStatus()
        );

        assertEquals(
                ScreeningSeatStatus.HELD,
                screeningSeat2.getStatus()
        );

        assertNotNull(screeningSeat1.getReservedUntil());

        assertEquals(
                screeningSeat1.getReservedUntil(),
                screeningSeat2.getReservedUntil()
        );

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void expirePendingBooking_shouldExpireBookingAndReleaseHeldSeats() {
        Long bookingId = 5L;
        ScreeningSeat screeningSeat1 = new ScreeningSeat();
        screeningSeat1.setStatus(ScreeningSeatStatus.HELD);
        screeningSeat1.setReservedUntil(
                LocalDateTime.now().minusMinutes(1)
        );

        ScreeningSeat screeningSeat2 = new ScreeningSeat();
        screeningSeat2.setStatus(ScreeningSeatStatus.HELD);
        screeningSeat2.setReservedUntil(
                LocalDateTime.now().minusMinutes(1)
        );

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        BookingItem item1 = new BookingItem();
        item1.setScreeningSeat(screeningSeat1);

        BookingItem item2 = new BookingItem();
        item2.setScreeningSeat(screeningSeat2);

        booking.addBookingItem(item1);
        booking.addBookingItem(item2);

        when(bookingRepository.findByIdWithLock(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.expirePendingBooking(bookingId);

        assertEquals(
                BookingStatus.EXPIRED,
                booking.getStatus()
        );

        assertEquals(
                ScreeningSeatStatus.AVAILABLE,
                screeningSeat1.getStatus()
        );

        assertEquals(
                ScreeningSeatStatus.AVAILABLE,
                screeningSeat2.getStatus()
        );

        assertNull(screeningSeat1.getReservedUntil());
        assertNull(screeningSeat2.getReservedUntil());

        verify(paymentService)
                .cancelExpiredBookingPayment(bookingId);
        verify(bookingRepository).findByIdWithLock(bookingId);
    }

    @Test
    void findExpiredPendingBookingIds_shouldReturnRepositoryResult() {
        when(bookingRepository.findExpiredPendingBookingIds(
                eq(BookingStatus.PENDING_PAYMENT),
                any(LocalDateTime.class)
        )).thenReturn(List.of(4L, 7L));

        List<Long> result = bookingService.findExpiredPendingBookingIds();

        assertEquals(List.of(4L, 7L), result);
        verify(bookingRepository).findExpiredPendingBookingIds(
                eq(BookingStatus.PENDING_PAYMENT),
                any(LocalDateTime.class)
        );
    }

    @Test
    void expirePendingBooking_shouldDoNothingWhenBookingNoLongerExists() {
        when(bookingRepository.findByIdWithLock(5L))
                .thenReturn(Optional.empty());

        bookingService.expirePendingBooking(5L);

        verifyNoInteractions(paymentService);
    }

    @Test
    void expirePendingBooking_shouldDoNothingWhenBookingIsNotPending() {
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(bookingRepository.findByIdWithLock(5L))
                .thenReturn(Optional.of(booking));

        bookingService.expirePendingBooking(5L);

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verifyNoInteractions(paymentService);
    }

    @Test
    void expirePendingBooking_shouldDoNothingWhenBookingHasNotExpired() {
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(bookingRepository.findByIdWithLock(5L))
                .thenReturn(Optional.of(booking));

        bookingService.expirePendingBooking(5L);

        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getStatus());
        verifyNoInteractions(paymentService);
    }

    @Test
    void getBookingById_shouldReturnOwnedBooking() {
        Cinema cinema = new Cinema();
        cinema.setName("Central Cinema");

        Hall hall = new Hall();
        hall.setName("Hall 1");
        hall.setCinema(cinema);

        Movie movie = new Movie();
        movie.setTitle("Interstellar");

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setStartTime(
                LocalDateTime.of(2026, 9, 1, 20, 0)
        );

        Seat seat = new Seat();
        seat.setRowLabel("A");
        seat.setSeatNumber(1);
        seat.setSeatType(SeatType.STANDARD);

        ScreeningSeat screeningSeat = new ScreeningSeat();
        screeningSeat.setId(10L);
        screeningSeat.setSeat(seat);

        BookingItem bookingItem = new BookingItem();
        bookingItem.setScreeningSeat(screeningSeat);
        bookingItem.setPrice(new BigDecimal("800.00"));

        Booking booking = Booking.builder()
                .id(5L)
                .bookingReference("BK-123")
                .screening(screening)
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .totalPrice(new BigDecimal("800.00"))
                .build();

        booking.addBookingItem(bookingItem);

        when(bookingRepository.findByIdAndUserId(5L, 1L))
                .thenReturn(Optional.of(booking));

        BookingResponseDTO response =
                bookingService.getBookingById(5L, 1L);

        assertAll(
                () -> assertEquals(5L, response.getId()),
                () -> assertEquals(
                        "BK-123",
                        response.getBookingReference()
                ),
                () -> assertEquals(
                        "Interstellar",
                        response.getMovieTitle()
                ),
                () -> assertEquals(
                        "Central Cinema",
                        response.getCinemaName()
                ),
                () -> assertEquals(
                        "Hall 1",
                        response.getHallName()
                ),
                () -> assertEquals(
                        new BigDecimal("800.00"),
                        response.getTotalPrice()
                ),
                () -> assertEquals(
                        1,
                        response.getSelectedSeats().size()
                )
        );

        verify(bookingRepository)
                .findByIdAndUserId(5L, 1L);
    }

    @Test
    void getBookingById_shouldThrowNotFoundWhenBookingIsNotOwned(){
        Long bookingId = 5L;
        Long userId = 1L;
        when(bookingRepository.findByIdAndUserId(bookingId, userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.getBookingById(bookingId, userId)
        );

        assertEquals(
                "Booking with ID " + bookingId + " was not found",
                exception.getMessage()
        );

        verify(bookingRepository)
                .findByIdAndUserId(bookingId, userId);
    }

    @Test
    void cancelBooking_shouldCancelBookingAndReleaseHeldSeats(){
        Long bookingId = 5L;
        Long userId = 1L;

        ScreeningSeat screeningSeat1 = new ScreeningSeat();
        screeningSeat1.setStatus(ScreeningSeatStatus.HELD);
        screeningSeat1.setReservedUntil(
                LocalDateTime.now().minusMinutes(10)
        );

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        BookingItem item1 = new BookingItem();
        item1.setScreeningSeat(screeningSeat1);

        booking.addBookingItem(item1);

        when(bookingRepository.findByIdAndUserId(bookingId, userId))
                .thenReturn(Optional.of(booking));


        bookingService.cancelBooking(bookingId, userId);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(ScreeningSeatStatus.AVAILABLE,
                screeningSeat1.getStatus());
        assertNull(screeningSeat1.getReservedUntil());

        verify(bookingRepository)
                .findByIdAndUserId(bookingId,userId);
        verify(paymentService)
                .cancelOpenPaymentForBooking(bookingId);
    }

    @Test
    void cancelBooking_shouldThrowNotFoundWhenBookingIsNotOwned(){
        when(bookingRepository.findByIdAndUserId(5L, 1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.cancelBooking(5L, 1L)
        );

        assertEquals(
                "Booking with ID 5 was not found",
                exception.getMessage()
        );

        verify(bookingRepository)
                .findByIdAndUserId(5L, 1L);
    }

    @Test
    void cancelBooking_shouldRejectBookingWithInvalidStatus(){
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByIdAndUserId(5L, 1L))
                .thenReturn(Optional.of(booking));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingService.cancelBooking(5L, 1L)
        );

        assertEquals(
                "Only pending payment bookings can be cancelled",
                exception.getMessage()
        );
        assertEquals(
                BookingStatus.CONFIRMED,
                booking.getStatus()
        );

    }

    @Test
    void getAllBookings_shouldReturnCustomerBookings() {
        Booking booking = createBookingForHistory();

        when(bookingRepository
                .findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(booking));

        List<BookingResponseDTO> response =
                bookingService.getAllBookings(1L);

        assertEquals(1, response.size());

        BookingResponseDTO returnedBooking = response.get(0);

        assertAll(
                () -> assertEquals(5L, returnedBooking.getId()),
                () -> assertEquals(
                        "BK-123",
                        returnedBooking.getBookingReference()
                ),
                () -> assertEquals(
                        "Interstellar",
                        returnedBooking.getMovieTitle()
                ),
                () -> assertEquals(
                        "Central Cinema",
                        returnedBooking.getCinemaName()
                ),
                () -> assertEquals(
                        "Hall 1",
                        returnedBooking.getHallName()
                ),
                () -> assertEquals(
                        BookingStatus.PENDING_PAYMENT,
                        returnedBooking.getStatus()
                ),
                () -> assertEquals(
                        new BigDecimal("800.00"),
                        returnedBooking.getTotalPrice()
                ),
                () -> assertEquals(
                        1,
                        returnedBooking.getSelectedSeats().size()
                )
        );

        verify(bookingRepository)
                .findAllByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getAllBookings_shouldReturnEmptyListWhenCustomerHasNoBookings() {
        when(bookingRepository
                .findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<BookingResponseDTO> response =
                bookingService.getAllBookings(1L);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(bookingRepository)
                .findAllByUserIdOrderByCreatedAtDesc(1L);
    }

    private Booking createBookingForHistory() {
        Cinema cinema = new Cinema();
        cinema.setName("Central Cinema");

        Hall hall = new Hall();
        hall.setName("Hall 1");
        hall.setCinema(cinema);

        Movie movie = new Movie();
        movie.setTitle("Interstellar");

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setHall(hall);
        screening.setStartTime(
                LocalDateTime.of(2026, 9, 1, 20, 0)
        );

        Seat seat = new Seat();
        seat.setRowLabel("A");
        seat.setSeatNumber(1);
        seat.setSeatType(SeatType.STANDARD);

        ScreeningSeat screeningSeat = new ScreeningSeat();
        screeningSeat.setId(10L);
        screeningSeat.setSeat(seat);

        BookingItem bookingItem = new BookingItem();
        bookingItem.setScreeningSeat(screeningSeat);
        bookingItem.setPrice(new BigDecimal("800.00"));

        Booking booking = Booking.builder()
                .id(5L)
                .bookingReference("BK-123")
                .screening(screening)
                .status(BookingStatus.PENDING_PAYMENT)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .totalPrice(new BigDecimal("800.00"))
                .build();

        booking.addBookingItem(bookingItem);

        return booking;
    }
}
