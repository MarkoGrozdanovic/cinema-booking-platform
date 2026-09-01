import type { AuthResponse } from "../../types/auth";

const AUTH_SESSION_KEY = "cinema-booking-auth";

export function saveAuthSession(session: AuthResponse) {
  sessionStorage.setItem(AUTH_SESSION_KEY, JSON.stringify(session));
}

export function loadAuthSession(): AuthResponse | null {
  const storedSession = sessionStorage.getItem(AUTH_SESSION_KEY);

  if (!storedSession) {
    return null;
  }

  try {
    return JSON.parse(storedSession) as AuthResponse;
  } catch {
    sessionStorage.removeItem(AUTH_SESSION_KEY);
    return null;
  }
}

export function clearAuthSession() {
  sessionStorage.removeItem(AUTH_SESSION_KEY);
}
