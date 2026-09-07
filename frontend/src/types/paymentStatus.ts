import type { BookingStatus } from "./booking";
import type { PaymentStatus } from "./payment";

export interface PaymentStatusResponse {
  bookingId: number;
  bookingStatus: BookingStatus;
  paymentStatus: PaymentStatus | null;
}
