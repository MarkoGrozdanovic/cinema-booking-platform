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
