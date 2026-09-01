export type AppRole = "CUSTOMER" | "ADMIN";

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface RegisteredUserResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: AppRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: AppRole;
  accessToken: string;
  tokenType: string;
}

export interface ApiErrorResponse {
  status: number;
  message: string;
  timestamp: string;
  errors?: Record<string, string>;
}

export interface AuthUser {
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: AppRole;
}
