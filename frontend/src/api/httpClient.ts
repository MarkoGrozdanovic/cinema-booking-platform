import axios from "axios";
import { store } from "../store/store";

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

export default httpClient;
