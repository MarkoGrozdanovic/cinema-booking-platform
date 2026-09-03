import httpClient from "./httpClient";
import type { CreateHallRequest, Hall } from "../types/hall";

export async function getAllHalls(): Promise<Hall[]> {
  const response = await httpClient.get<Hall[]>("/admin/halls");

  return response.data;
}

export async function createHall(request: CreateHallRequest): Promise<Hall> {
  const response = await httpClient.post<Hall>("/admin/halls", request);

  return response.data;
}
