export type BookingStatus =
  | "PENDING_PAYMENT"
  | "CONFIRMED"
  | "CANCELLED"
  | "EXPIRED";

export type PaymentStatus = "PENDING" | "SUCCEEDED" | "FAILED" | "CANCELLED";

export interface AdminBooking {
  id: number;
  bookingReference: string;

  customerId: number;
  customerName: string;
  customerEmail: string;

  movieTitle: string;
  cinemaName: string;
  hallName: string;
  screeningStartTime: string;

  bookingStatus: BookingStatus;
  paymentStatus: PaymentStatus | null;

  totalPrice: number;
  createdAt: string;
}
