import axios from "axios";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router";
import { registerUser } from "../api/authApi";
import type { ApiErrorResponse, RegisterRequest } from "../types/auth";

function RegisterPage() {
  const navigate = useNavigate();
  const [apiError, setApiError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<RegisterRequest>();

  async function onSubmit(request: RegisterRequest) {
    setApiError(null);

    try {
      await registerUser(request);

      navigate("/login", {
        state: {
          message: "Registration successful. You can now log in.",
        },
      });
    } catch (error) {
      if (axios.isAxiosError<ApiErrorResponse>(error) && error.response) {
        const response = error.response.data;

        if (response.errors) {
          Object.entries(response.errors).forEach(([field, message]) => {
            setError(field as keyof RegisterRequest, {
              type: "server",
              message,
            });
          });
        } else {
          setApiError(response.message);
        }

        return;
      }

      setApiError("Unable to register. Please try again.");
    }
  }

  return (
    <section className="mx-auto max-w-md">
      <div className="rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl">
        <h1 className="text-3xl font-bold">Create an account</h1>

        <p className="mt-2 text-slate-400">Register to reserve cinema seats.</p>

        {apiError && (
          <div className="mt-6 rounded-lg border border-red-800 bg-red-950/50 p-3 text-sm text-red-300">
            {apiError}
          </div>
        )}

        <form className="mt-8 space-y-5" onSubmit={handleSubmit(onSubmit)}>
          <div>
            <label
              className="mb-2 block text-sm font-medium"
              htmlFor="firstName"
            >
              First name
            </label>

            <input
              id="firstName"
              autoComplete="given-name"
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 outline-none transition focus:border-red-500"
              {...register("firstName", {
                required: "First name is required",
                maxLength: {
                  value: 100,
                  message: "First name cannot exceed 100 characters",
                },
              })}
            />

            {errors.firstName && (
              <p className="mt-1 text-sm text-red-400">
                {errors.firstName.message}
              </p>
            )}
          </div>

          <div>
            <label
              className="mb-2 block text-sm font-medium"
              htmlFor="lastName"
            >
              Last name
            </label>

            <input
              id="lastName"
              autoComplete="family-name"
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 outline-none transition focus:border-red-500"
              {...register("lastName", {
                required: "Last name is required",
                maxLength: {
                  value: 100,
                  message: "Last name cannot exceed 100 characters",
                },
              })}
            />

            {errors.lastName && (
              <p className="mt-1 text-sm text-red-400">
                {errors.lastName.message}
              </p>
            )}
          </div>

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
                maxLength: {
                  value: 255,
                  message: "Email cannot exceed 255 characters",
                },
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
              autoComplete="new-password"
              className="w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-3 outline-none transition focus:border-red-500"
              {...register("password", {
                required: "Password is required",
                minLength: {
                  value: 8,
                  message: "Password must contain at least 8 characters",
                },
                maxLength: {
                  value: 72,
                  message: "Password cannot exceed 72 characters",
                },
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
            {isSubmitting ? "Creating account..." : "Create account"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-400">
          Already have an account?{" "}
          <Link
            className="font-medium text-red-400 hover:text-red-300"
            to="/login"
          >
            Log in
          </Link>
        </p>
      </div>
    </section>
  );
}

export default RegisterPage;
