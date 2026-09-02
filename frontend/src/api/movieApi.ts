import httpClient from "./httpClient";
import type { CreateMovieRequest, Movie } from "../types/movie";

export async function getAllMovies(): Promise<Movie[]> {
  const response = await httpClient.get<Movie[]>("/admin/movies");

  return response.data;
}

export async function createMovie(request: CreateMovieRequest): Promise<Movie> {
  const response = await httpClient.post<Movie>("/admin/movies", request);

  return response.data;
}

export async function updateMovieStatus(
  movieId: number,
  active: boolean,
): Promise<Movie> {
  const response = await httpClient.put<Movie>(
    `/admin/movies/${movieId}/status`,
    {
      active,
    },
  );

  return response.data;
}
