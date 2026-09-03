export type HallType = "STANDARD" | "THREE_D" | "IMAX" | "VIP";

export type SeatType = "STANDARD" | "VIP" | "COUPLE" | "WHEELCHAIR_ACCESSIBLE";

export interface CreateSeatRowRequest {
  rowLabel: string;
  numberOfSeats: number;
  seatType: SeatType;
}

export interface CreateHallRequest {
  cinemaId: number;
  name: string;
  hallType: HallType;
  rows: CreateSeatRowRequest[];
}

export interface Hall {
  id: number;
  name: string;
  hallType: HallType;
  cinemaId: number;
  cinemaName: string;
  active: boolean;
  numberOfSeats: number;
}
