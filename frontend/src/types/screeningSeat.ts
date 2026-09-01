export type SeatType = "STANDARD" | "VIP" | "COUPLE" | "WHEELCHAIR_ACCESSIBLE";

export type ScreeningSeatStatus = "AVAILABLE" | "HELD" | "SOLD" | "BLOCKED";

export interface ScreeningSeat {
  screeningSeatId: number;
  rowLabel: string;
  seatNumber: number;
  seatType: SeatType;
  price: number;
  status: ScreeningSeatStatus;
  reservedUntil: string | null;
}
