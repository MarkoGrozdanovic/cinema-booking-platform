import { isAxiosError } from "axios";
import { useEffect, useState } from "react";
import { useForm, type SubmitHandler } from "react-hook-form";
import {
  createScreening,
  getActiveHallOptions,
  getActiveMovieOptions,
} from "../../api/screeningApi";
import type {
  CreateScreeningRequest,
  HallOption,
  MovieOption,
  Screening,
} from "../../types/screening";

function validateFutureStartTime(value: string): true | string {
  const selectedTime = new Date(value).getTime();
  const currentTime = new Date().getTime();

  return selectedTime > currentTime || "Start time must be in the future.";
}

function CreateScreeningPage() {
  const [createdScreening, setCreatedScreening] = useState<Screening | null>(
    null,
  );

  const [submitError, setSubmitError] = useState<string | null>(null);

  const [movieOptions, setMovieOptions] = useState<MovieOption[]>([]);

  const [hallOptions, setHallOptions] = useState<HallOption[]>([]);

  const [isLoadingOptions, setIsLoadingOptions] = useState(true);

  const [optionsError, setOptionsError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CreateScreeningRequest>();

  useEffect(() => {
    let cancelled = false;

    async function loadOptions() {
      try {
        const [movies, halls] = await Promise.all([
          getActiveMovieOptions(),
          getActiveHallOptions(),
        ]);

        if (!cancelled) {
          setMovieOptions(movies);
          setHallOptions(halls);
        }
      } catch {
        if (!cancelled) {
          setOptionsError("Unable to load movies and cinema halls.");
        }
      } finally {
        if (!cancelled) {
          setIsLoadingOptions(false);
        }
      }
    }

    void loadOptions();

    return () => {
      cancelled = true;
    };
  }, []);

  const onSubmit: SubmitHandler<CreateScreeningRequest> = async (request) => {
    setSubmitError(null);
    setCreatedScreening(null);

    try {
      const screening = await createScreening(request);

      setCreatedScreening(screening);
      reset();
    } catch (error) {
      if (isAxiosError<{ message?: string }>(error)) {
        setSubmitError(
          error.response?.data.message ?? "Unable to create screening.",
        );
      } else {
        setSubmitError("Unable to create screening.");
      }
    }
  };

  return (
    <section className="mx-auto max-w-2xl">
      <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
        Administration
      </p>

      <h1 className="mt-2 text-3xl font-bold">Create screening</h1>

      <p className="mt-3 text-slate-400">
        Schedule a movie in an active cinema hall.
      </p>

      <form
        onSubmit={(event) => void handleSubmit(onSubmit)(event)}
        className="mt-8 space-y-5 rounded-xl border border-slate-700 bg-slate-800 p-6"
      >
        <div>
          <label
            htmlFor="movieId"
            className="block text-sm font-medium text-slate-200"
          >
            Movie
          </label>

          <select
            id="movieId"
            disabled={isLoadingOptions}
            {...register("movieId", {
              required: "Movie is required.",
              valueAsNumber: true,
            })}
            className="mt-2 w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 outline-none transition focus:border-amber-400 disabled:opacity-60"
          >
            <option value="">
              {isLoadingOptions ? "Loading movies..." : "Select a movie"}
            </option>

            {movieOptions.map((movie) => (
              <option key={movie.id} value={movie.id}>
                {movie.title} ({movie.durationMinutes} min)
              </option>
            ))}
          </select>

          {errors.movieId && (
            <p className="mt-2 text-sm text-red-300">
              {errors.movieId.message}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="hallId"
            className="block text-sm font-medium text-slate-200"
          >
            Cinema hall
          </label>

          <select
            id="hallId"
            disabled={isLoadingOptions}
            {...register("hallId", {
              required: "Cinema hall is required.",
              valueAsNumber: true,
            })}
            className="mt-2 w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 outline-none transition focus:border-amber-400 disabled:opacity-60"
          >
            <option value="">
              {isLoadingOptions ? "Loading halls..." : "Select a cinema hall"}
            </option>

            {hallOptions.map((hall) => (
              <option key={hall.id} value={hall.id}>
                {hall.cinemaName} — {hall.hallName}
              </option>
            ))}
          </select>

          {errors.hallId && (
            <p className="mt-2 text-sm text-red-300">{errors.hallId.message}</p>
          )}
        </div>

        <div>
          <label
            htmlFor="startTime"
            className="block text-sm font-medium text-slate-200"
          >
            Start time
          </label>

          <input
            id="startTime"
            type="datetime-local"
            {...register("startTime", {
              required: "Start time is required.",
              validate: validateFutureStartTime,
            })}
            className="mt-2 w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 outline-none transition focus:border-amber-400"
          />

          {errors.startTime && (
            <p className="mt-2 text-sm text-red-300">
              {errors.startTime.message}
            </p>
          )}
        </div>

        <div>
          <label
            htmlFor="basePrice"
            className="block text-sm font-medium text-slate-200"
          >
            Base price (RSD)
          </label>

          <input
            id="basePrice"
            type="number"
            min="1"
            step="0.01"
            {...register("basePrice", {
              required: "Base price is required.",
              valueAsNumber: true,
              min: {
                value: 1,
                message: "Base price must be greater than zero.",
              },
            })}
            className="mt-2 w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 outline-none transition focus:border-amber-400"
          />

          {errors.basePrice && (
            <p className="mt-2 text-sm text-red-300">
              {errors.basePrice.message}
            </p>
          )}
        </div>

        {submitError && (
          <div className="rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
            {submitError}
          </div>
        )}

        {optionsError && (
          <div className="rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
            {optionsError}
          </div>
        )}

        <button
          type="submit"
          disabled={isSubmitting || isLoadingOptions || optionsError !== null}
          className="w-full rounded-lg bg-amber-500 px-4 py-3 font-semibold text-slate-950 transition hover:bg-amber-400 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSubmitting ? "Creating screening..." : "Create screening"}
        </button>
      </form>

      {createdScreening && (
        <div className="mt-6 rounded-xl border border-emerald-500/40 bg-emerald-950/30 p-5 text-emerald-200">
          <p className="font-semibold">Screening created successfully.</p>

          <p className="mt-2 text-sm">
            {createdScreening.movieTitle} at{" "}
            {new Date(createdScreening.startTime).toLocaleString()}
          </p>

          <p className="mt-1 text-sm">
            {createdScreening.numberOfSeats} screening seats generated.
          </p>
        </div>
      )}
    </section>
  );
}

export default CreateScreeningPage;
