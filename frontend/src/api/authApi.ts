import httpClient from "./httpClient";
import type {
  AuthResponse,
  LoginRequest,
  RegisteredUserResponse,
  RegisterRequest,
} from "../types/auth";

export async function registerUser(
  request: RegisterRequest,
): Promise<RegisteredUserResponse> {
  const response = await httpClient.post<RegisteredUserResponse>(
    "/auth/register",
    request,
  );

  return response.data;
}

export async function loginUser(request: LoginRequest): Promise<AuthResponse> {
  const response = await httpClient.post<AuthResponse>("/auth/login", request);

  return response.data;
}
