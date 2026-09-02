import httpClient from "./httpClient";
import type { HallOption, MovieOption, Screening } from "../types/screening";
import type { ScreeningSeat } from "../types/screeningSeat";
import type { CreateScreeningRequest } from "../types/screening";

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

export async function createScreening(
  request: CreateScreeningRequest,
): Promise<Screening> {
  const response = await httpClient.post<Screening>("/screenings", request);

  return response.data;
}

export async function getActiveMovieOptions(): Promise<MovieOption[]> {
  const response = await httpClient.get<MovieOption[]>(
    "/screenings/options/movies",
  );

  return response.data;
}

export async function getActiveHallOptions(): Promise<HallOption[]> {
  const response = await httpClient.get<HallOption[]>(
    "/screenings/options/halls",
  );

  return response.data;
}
