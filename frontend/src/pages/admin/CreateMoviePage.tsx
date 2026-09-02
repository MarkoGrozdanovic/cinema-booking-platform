import { isAxiosError } from "axios";
import { useForm, type SubmitHandler } from "react-hook-form";
import { useNavigate } from "react-router";
import { createMovie } from "../../api/movieApi";
import type { AgeRating, CreateMovieRequest, Genre } from "../../types/movie";

const ageRatings: AgeRating[] = [
  "GENERAL",
  "SEVEN_PLUS",
  "TWELVE_PLUS",
  "SIXTEEN_PLUS",
  "EIGHTEEN_PLUS",
];

const genres: Genre[] = [
  "ACTION",
  "COMEDY",
  "DRAMA",
  "HORROR",
  "THRILLER",
  "SCIENCE_FICTION",
  "FANTASY",
  "ANIMATION",
  "DOCUMENTARY",
];

function validateOptionalUrl(value: string | undefined): true | string {
  if (!value) {
    return true;
  }

  try {
    const url = new URL(value);

    return (
      url.protocol === "http:" ||
      url.protocol === "https:" ||
      "URL must use HTTP or HTTPS."
    );
  } catch {
    return "Enter a valid URL.";
  }
}

function CreateMoviePage() {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<CreateMovieRequest>();

  const onSubmit: SubmitHandler<CreateMovieRequest> = async (request) => {
    try {
      await createMovie(request);
      navigate("/admin/movies");
    } catch (error) {
      const message = isAxiosError<{ message?: string }>(error)
        ? error.response?.data.message
        : undefined;

      setError("root", {
        message: message ?? "Unable to create movie.",
      });
    }
  };

  const inputClassName =
    "mt-2 w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-3 outline-none transition focus:border-amber-400";

  return (
    <section className="mx-auto max-w-3xl">
      <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
        Administration
      </p>

      <h1 className="mt-2 text-3xl font-bold">Add movie</h1>

      <p className="mt-3 text-slate-400">
        Add a movie that can be assigned to screenings.
      </p>

      <form
        onSubmit={(event) => void handleSubmit(onSubmit)(event)}
        className="mt-8 space-y-6 rounded-xl border border-slate-700 bg-slate-800 p-6"
      >
        <div>
          <label htmlFor="title">Title</label>

          <input
            id="title"
            {...register("title", {
              required: "Title is required.",
              maxLength: {
                value: 150,
                message: "Title must not exceed 150 characters.",
              },
            })}
            className={inputClassName}
          />

          {errors.title && (
            <p className="mt-2 text-sm text-red-300">{errors.title.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="description">Description</label>

          <textarea
            id="description"
            rows={4}
            {...register("description", {
              maxLength: {
                value: 1000,
                message: "Description must not exceed 1000 characters.",
              },
            })}
            className={inputClassName}
          />

          {errors.description && (
            <p className="mt-2 text-sm text-red-300">
              {errors.description.message}
            </p>
          )}
        </div>

        <div className="grid gap-5 md:grid-cols-2">
          <div>
            <label htmlFor="durationMinutes">Duration (minutes)</label>

            <input
              id="durationMinutes"
              type="number"
              min="1"
              {...register("durationMinutes", {
                required: "Duration is required.",
                valueAsNumber: true,
                min: {
                  value: 1,
                  message: "Duration must be greater than zero.",
                },
              })}
              className={inputClassName}
            />

            {errors.durationMinutes && (
              <p className="mt-2 text-sm text-red-300">
                {errors.durationMinutes.message}
              </p>
            )}
          </div>

          <div>
            <label htmlFor="releaseDate">Release date</label>

            <input
              id="releaseDate"
              type="date"
              {...register("releaseDate", {
                required: "Release date is required.",
              })}
              className={inputClassName}
            />

            {errors.releaseDate && (
              <p className="mt-2 text-sm text-red-300">
                {errors.releaseDate.message}
              </p>
            )}
          </div>
        </div>

        <div className="grid gap-5 md:grid-cols-2">
          <div>
            <label htmlFor="ageRating">Age rating</label>

            <select
              id="ageRating"
              {...register("ageRating", {
                required: "Age rating is required.",
              })}
              className={inputClassName}
            >
              <option value="">Select an age rating</option>

              {ageRatings.map((rating) => (
                <option key={rating} value={rating}>
                  {rating.replaceAll("_", " ")}
                </option>
              ))}
            </select>

            {errors.ageRating && (
              <p className="mt-2 text-sm text-red-300">
                {errors.ageRating.message}
              </p>
            )}
          </div>

          <div>
            <label htmlFor="genre">Genre</label>

            <select
              id="genre"
              {...register("genre", {
                required: "Genre is required.",
              })}
              className={inputClassName}
            >
              <option value="">Select a genre</option>

              {genres.map((genre) => (
                <option key={genre} value={genre}>
                  {genre.replaceAll("_", " ")}
                </option>
              ))}
            </select>

            {errors.genre && (
              <p className="mt-2 text-sm text-red-300">
                {errors.genre.message}
              </p>
            )}
          </div>
        </div>

        <div className="grid gap-5 md:grid-cols-2">
          <div>
            <label htmlFor="language">Language</label>

            <input
              id="language"
              {...register("language", {
                required: "Language is required.",
                maxLength: {
                  value: 50,
                  message: "Language must not exceed 50 characters.",
                },
              })}
              className={inputClassName}
            />

            {errors.language && (
              <p className="mt-2 text-sm text-red-300">
                {errors.language.message}
              </p>
            )}
          </div>

          <div>
            <label htmlFor="director">Director</label>

            <input
              id="director"
              {...register("director", {
                required: "Director is required.",
                maxLength: {
                  value: 100,
                  message: "Director must not exceed 100 characters.",
                },
              })}
              className={inputClassName}
            />

            {errors.director && (
              <p className="mt-2 text-sm text-red-300">
                {errors.director.message}
              </p>
            )}
          </div>
        </div>

        <div>
          <label htmlFor="posterUrl">Poster URL</label>

          <input
            id="posterUrl"
            type="url"
            {...register("posterUrl", {
              validate: validateOptionalUrl,
              maxLength: {
                value: 500,
                message: "Poster URL must not exceed 500 characters.",
              },
            })}
            className={inputClassName}
          />

          {errors.posterUrl && (
            <p className="mt-2 text-sm text-red-300">
              {errors.posterUrl.message}
            </p>
          )}
        </div>

        <div>
          <label htmlFor="trailerUrl">Trailer URL</label>

          <input
            id="trailerUrl"
            type="url"
            {...register("trailerUrl", {
              validate: validateOptionalUrl,
              maxLength: {
                value: 500,
                message: "Trailer URL must not exceed 500 characters.",
              },
            })}
            className={inputClassName}
          />

          {errors.trailerUrl && (
            <p className="mt-2 text-sm text-red-300">
              {errors.trailerUrl.message}
            </p>
          )}
        </div>

        {errors.root && (
          <div className="rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
            {errors.root.message}
          </div>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-lg bg-amber-500 px-4 py-3 font-semibold text-slate-950 transition hover:bg-amber-400 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSubmitting ? "Creating movie..." : "Create movie"}
        </button>
      </form>
    </section>
  );
}

export default CreateMoviePage;
