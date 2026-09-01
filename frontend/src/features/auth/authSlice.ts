import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { AuthResponse, AuthUser } from "../../types/auth";
import { loadAuthSession } from "./authStorage";

interface AuthState {
  user: AuthUser | null;
  accessToken: string | null;
}

const storedSession = loadAuthSession();

const initialState: AuthState = {
  user: storedSession
    ? {
        userId: storedSession.userId,
        firstName: storedSession.firstName,
        lastName: storedSession.lastName,
        email: storedSession.email,
        role: storedSession.role,
      }
    : null,

  accessToken: storedSession?.accessToken ?? null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setCredentials(state, action: PayloadAction<AuthResponse>) {
      const response = action.payload;

      state.user = {
        userId: response.userId,
        firstName: response.firstName,
        lastName: response.lastName,
        email: response.email,
        role: response.role,
      };

      state.accessToken = response.accessToken;
    },

    logout(state) {
      state.user = null;
      state.accessToken = null;
    },
  },
});

export const { setCredentials, logout } = authSlice.actions;

export default authSlice.reducer;
