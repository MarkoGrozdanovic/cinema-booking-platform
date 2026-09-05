import httpClient from "./httpClient";
import type { AdminBooking } from "../types/adminBooking";

export async function getAdminBookings(): Promise<AdminBooking[]> {
  const response = await httpClient.get<AdminBooking[]>("/admin/bookings");

  return response.data;
}
