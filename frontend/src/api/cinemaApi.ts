import httpClient from "./httpClient";
import type { Cinema, CreateCinemaRequest } from "../types/cinema";

export async function getAllCinemas(): Promise<Cinema[]> {
  const response = await httpClient.get<Cinema[]>("/admin/cinemas");

  return response.data;
}

export async function createCinema(
  request: CreateCinemaRequest,
): Promise<Cinema> {
  const response = await httpClient.post<Cinema>("/admin/cinemas", request);

  return response.data;
}
