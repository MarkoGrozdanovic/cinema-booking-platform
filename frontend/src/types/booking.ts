export type BookingStatus =
  | "PENDING_PAYMENT"
  | "CONFIRMED"
  | "EXPIRED"
  | "CANCELLED"
  | "PAYMENT_FAILED";

export interface BookedSeat {
  screeningSeatId: number;
  rowLabel: string;
  seatNumber: number;
  seatType: string;
  price: number;
}

export interface Booking {
  id: number;
  bookingReference: string;
  movieTitle: string;
  cinemaName: string;
  hallName: string;
  status: BookingStatus;
  screeningStartTime: string;
  expiresAt: string;
  totalPrice: number;
  selectedSeats: BookedSeat[];
}

export interface CreateBookingRequest {
  screeningId: number;
  screeningSeatIds: number[];
}
