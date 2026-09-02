import { useEffect, useState } from "react";
import { Link } from "react-router";
import { getAllMovies, updateMovieStatus } from "../../api/movieApi";
import type { Movie } from "../../types/movie";

function AdminMoviesPage() {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [updatingMovieId, setUpdatingMovieId] = useState<number | null>(null);

  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadMovies() {
      try {
        const response = await getAllMovies();

        if (!cancelled) {
          setMovies(response);
        }
      } catch {
        if (!cancelled) {
          setError("Unable to load movies.");
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadMovies();

    return () => {
      cancelled = true;
    };
  }, []);

  async function handleStatusChange(movie: Movie) {
    if (movie.active && !window.confirm(`Deactivate "${movie.title}"?`)) {
      return;
    }

    setUpdatingMovieId(movie.id);
    setActionError(null);

    try {
      const updatedMovie = await updateMovieStatus(movie.id, !movie.active);

      setMovies((currentMovies) =>
        currentMovies.map((currentMovie) =>
          currentMovie.id === updatedMovie.id ? updatedMovie : currentMovie,
        ),
      );
    } catch {
      setActionError("Unable to update the movie status.");
    } finally {
      setUpdatingMovieId(null);
    }
  }

  return (
    <section>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
            Administration
          </p>

          <h1 className="mt-2 text-4xl font-bold">Movies</h1>

          <p className="mt-3 text-slate-400">
            Manage movies available for cinema screenings.
          </p>
        </div>

        <Link
          to="/admin/movies/new"
          className="rounded-lg bg-amber-500 px-5 py-3 font-semibold text-slate-950 transition hover:bg-amber-400"
        >
          Add movie
        </Link>
      </div>

      {isLoading && <p className="mt-10 text-slate-300">Loading movies...</p>}

      {error && (
        <div className="mt-8 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {error}
        </div>
      )}

      {!isLoading && !error && movies.length === 0 && (
        <div className="mt-8 rounded-xl border border-slate-700 bg-slate-800 p-6 text-slate-300">
          No movies have been added yet.
        </div>
      )}

      {actionError && (
        <div className="mt-8 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {actionError}
        </div>
      )}

      {!isLoading && !error && movies.length > 0 && (
        <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          {movies.map((movie) => (
            <article
              key={movie.id}
              className="overflow-hidden rounded-xl border border-slate-700 bg-slate-800"
            >
              {movie.posterUrl ? (
                <img
                  src={movie.posterUrl}
                  alt={`${movie.title} poster`}
                  className="h-72 w-full object-cover"
                />
              ) : (
                <div className="flex h-72 items-center justify-center bg-slate-700 text-slate-400">
                  No poster
                </div>
              )}

              <div className="p-5">
                <div className="flex items-start justify-between gap-3">
                  <h2 className="text-xl font-semibold">{movie.title}</h2>

                  <span
                    className={`rounded-full px-3 py-1 text-xs font-semibold ${
                      movie.active
                        ? "bg-emerald-950 text-emerald-300"
                        : "bg-slate-700 text-slate-400"
                    }`}
                  >
                    {movie.active ? "Active" : "Inactive"}
                  </span>
                </div>

                <p className="mt-3 text-sm text-slate-400">
                  {movie.genre.replaceAll("_", " ")} · {movie.durationMinutes}{" "}
                  min
                </p>

                <p className="mt-2 text-sm text-slate-400">
                  Directed by {movie.director}
                </p>

                <button
                  type="button"
                  disabled={updatingMovieId === movie.id}
                  onClick={() => void handleStatusChange(movie)}
                  className={`mt-5 w-full rounded-lg border px-4 py-2 font-semibold transition disabled:cursor-not-allowed disabled:opacity-60 ${
                    movie.active
                      ? "border-red-500 text-red-300 hover:bg-red-950/40"
                      : "border-emerald-500 text-emerald-300 hover:bg-emerald-950/40"
                  }`}
                >
                  {updatingMovieId === movie.id
                    ? "Updating..."
                    : movie.active
                      ? "Deactivate"
                      : "Activate"}
                </button>

                <p className="mt-2 text-sm text-slate-500">
                  Released{" "}
                  {new Date(
                    `${movie.releaseDate}T00:00:00`,
                  ).toLocaleDateString()}
                </p>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default AdminMoviesPage;
