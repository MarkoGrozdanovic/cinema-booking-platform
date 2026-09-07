import httpClient from "./httpClient";
import type {
  AdminBooking,
  BookingStatus,
  PaymentStatus,
} from "../types/adminBooking";
import type { PageResponse } from "../types/page";

export interface AdminBookingQuery {
  page: number;
  size: number;
  search?: string;
  bookingStatus?: BookingStatus;
  paymentStatus?: PaymentStatus;
  paymentNotStarted?: boolean;
}

export async function getAdminBookings(
  query: AdminBookingQuery,
): Promise<PageResponse<AdminBooking>> {
  const response = await httpClient.get<PageResponse<AdminBooking>>(
    "/admin/bookings",
    {
      params: query,
    },
  );

  return response.data;
}
