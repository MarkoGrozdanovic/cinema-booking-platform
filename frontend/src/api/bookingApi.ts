import type { Booking, CreateBookingRequest } from "../types/booking";
import httpClient from "./httpClient";

export async function getMyBookings(signal?: AbortSignal): Promise<Booking[]> {
  const response = await httpClient.get<Booking[]>("/bookings", { signal });

  return response.data;
}

export async function createBooking(
  request: CreateBookingRequest,
): Promise<Booking> {
  const response = await httpClient.post<Booking>("/bookings", request);

  return response.data;
}
