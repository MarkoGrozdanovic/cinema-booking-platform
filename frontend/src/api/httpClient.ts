import axios from "axios";
import { store } from "../store/store";
import { logout } from "../features/auth/authSlice";

const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10_000,
});

httpClient.interceptors.request.use((config) => {
  const accessToken = store.getState().auth.accessToken;

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 401 &&
      store.getState().auth.user
    ) {
      store.dispatch(logout());
    }

    return Promise.reject(error);
  },
);

export default httpClient;
