import httpClient from "./httpClient";
import type { Screening } from "../types/screening";
import type { ScreeningSeat } from "../types/screeningSeat";

export async function getUpcomingScreenings(): Promise<Screening[]> {
  const response = await httpClient.get<Screening[]>("/screenings/upcoming");

  return response.data;
}

export async function getScreeningSeats(
  screeningId: number,
): Promise<ScreeningSeat[]> {
  const response = await httpClient.get<ScreeningSeat[]>(
    `/screenings/${screeningId}/seats`,
  );

  return response.data;
}
