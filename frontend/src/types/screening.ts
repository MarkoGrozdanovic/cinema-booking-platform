export type ScreeningStatus = "SCHEDULED" | "CANCELLED" | "COMPLETED";

export interface Screening {
  id: number;
  movieId: number;
  movieTitle: string;
  hallId: number;
  cinemaName: string;
  startTime: string;
  endTime: string;
  basePrice: number;
  status: ScreeningStatus;
  numberOfSeats: number;
}

export interface CreateScreeningRequest {
  movieId: number;
  hallId: number;
  startTime: string;
  basePrice: number;
}

export interface MovieOption {
  id: number;
  title: string;
  durationMinutes: number;
}

export interface HallOption {
  id: number;
  hallName: string;
  cinemaName: string;
}
