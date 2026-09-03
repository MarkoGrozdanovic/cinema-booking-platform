export interface CreateCinemaRequest {
  name: string;
  address: string;
  city: string;
  description?: string;
}

export interface Cinema {
  id: number;
  name: string;
  address: string;
  city: string;
  description: string | null;
  active: boolean;
}
