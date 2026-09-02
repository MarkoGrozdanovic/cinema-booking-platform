export type AgeRating =
  | "GENERAL"
  | "SEVEN_PLUS"
  | "TWELVE_PLUS"
  | "SIXTEEN_PLUS"
  | "EIGHTEEN_PLUS";

export type Genre =
  | "ACTION"
  | "COMEDY"
  | "DRAMA"
  | "HORROR"
  | "THRILLER"
  | "SCIENCE_FICTION"
  | "FANTASY"
  | "ANIMATION"
  | "DOCUMENTARY";

export interface CreateMovieRequest {
  title: string;
  description?: string;
  durationMinutes: number;
  releaseDate: string;
  ageRating: AgeRating;
  genre: Genre;
  language: string;
  director: string;
  posterUrl?: string;
  trailerUrl?: string;
}

export interface Movie {
  id: number;
  title: string;
  description: string | null;
  durationMinutes: number;
  releaseDate: string;
  ageRating: AgeRating;
  genre: Genre;
  language: string;
  director: string;
  posterUrl: string | null;
  trailerUrl: string | null;
  active: boolean;
}
