import axios from "axios";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useLocation, useNavigate } from "react-router";
import { loginUser } from "../api/authApi";
import { setCredentials } from "../features/auth/authSlice";
import { useAppDispatch } from "../store/hooks";
import type { ApiErrorResponse, LoginRequest } from "../types/auth";
import { saveAuthSession } from "../features/auth/authStorage";

interface LocationState {
  message?: string;
  from?: string;
}

function LoginPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();

  const locationState = location.state as LocationState | null;

  const [apiError, setApiError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginRequest>();

  async function onSubmit(request: LoginRequest) {
    setApiError(null);

    try {
      const response = await loginUser(request);

      saveAuthSession(response);
      dispatch(setCredentials(response));

      navigate(locationState?.from ?? "/", {
        replace: true,
      });
    } catch (error) {
      if (axios.isAxiosError<ApiErrorResponse>(error) && error.response) {
        setApiError(error.response.data.message);
        return;
      }

      setApiError("Unable to log in. Please try again.");
    }
  }

  return (
    <section className="mx-auto max-w-md">
      <div className="rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl">
        <h1 className="text-3xl font-bold">Welcome back</h1>

        <p className="mt-2 text-slate-400">Log in to manage your bookings.</p>

        {locationState?.message && (
          <div className="mt-6 rounded-lg border border-emerald-800 bg-emerald-950/50 p-3 text-sm text-emerald-300">
            {locationState.message}
          </div>
        )}

        {apiError && (
          <div className="mt-6 rounded-lg border border-red-800 bg-red-950/50 p-3 text-sm text-red-300">
            {apiError}
          </div>
        )}

        <form className="mt-8 space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label className="mb-2 block text-sm font-medium" htmlFor="email">
              Email
            </label>

            <input
              id="email"
              type="email"
              autoComplete="email"
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 outline-none transition focus:border-red-500"
              {...register("email", {
                required: "Email is required",
              })}
            />

            {errors.email && (
              <p className="mt-1 text-sm text-red-400">
                {errors.email.message}
              </p>
            )}
          </div>

          <div>
            <label
              className="mb-2 block text-sm font-medium"
              htmlFor="password"
            >
              Password
            </label>

            <input
              id="password"
              type="password"
              autoComplete="current-password"
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 outline-none transition focus:border-red-500"
              {...register("password", {
                required: "Password is required",
              })}
            />

            {errors.password && (
              <p className="mt-1 text-sm text-red-400">
                {errors.password.message}
              </p>
            )}
          </div>

          <button
            className="w-full rounded-lg bg-red-600 px-4 py-3 font-semibold transition hover:bg-red-500 disabled:cursor-not-allowed disabled:opacity-60"
            type="submit"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Logging in..." : "Log in"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-400">
          Don&apos;t have an account?{" "}
          <Link
            className="font-medium text-red-400 hover:text-red-300"
            to="/register"
          >
            Register
          </Link>
        </p>
      </div>
    </section>
  );
}

export default LoginPage;
