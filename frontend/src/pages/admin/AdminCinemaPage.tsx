import { useEffect, useState } from "react";
import { Link } from "react-router";
import { getAllCinemas } from "../../api/cinemaApi";
import type { Cinema } from "../../types/cinema";
import { isAxiosError } from "axios";
import { updateCinemaStatus } from "../../api/cinemaApi";

function AdminCinemasPage() {
  const [cinemas, setCinemas] = useState<Cinema[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [updatingCinemaId, setUpdatingCinemaId] = useState<number | null>(null);

  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadCinemas() {
      try {
        const response = await getAllCinemas();

        if (!cancelled) {
          setCinemas(response);
        }
      } catch {
        if (!cancelled) {
          setError("Unable to load cinemas.");
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadCinemas();

    return () => {
      cancelled = true;
    };
  }, []);

  async function handleStatusChange(cinema: Cinema) {
    if (cinema.active && !window.confirm(`Deactivate "${cinema.name}"?`)) {
      return;
    }

    setUpdatingCinemaId(cinema.id);
    setActionError(null);

    try {
      const updatedCinema = await updateCinemaStatus(cinema.id, !cinema.active);

      setCinemas((currentCinemas) =>
        currentCinemas.map((currentCinema) =>
          currentCinema.id === updatedCinema.id ? updatedCinema : currentCinema,
        ),
      );
    } catch (error) {
      const message = isAxiosError<{ message?: string }>(error)
        ? error.response?.data.message
        : undefined;

      setActionError(message ?? "Unable to update cinema status.");
    } finally {
      setUpdatingCinemaId(null);
    }
  }

  return (
    <section>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
            Administration
          </p>

          <h1 className="mt-2 text-4xl font-bold">Cinemas</h1>

          <p className="mt-3 text-slate-400">Manage cinema locations.</p>
        </div>

        <Link
          to="/admin/cinemas/new"
          className="rounded-lg bg-amber-500 px-5 py-3 font-semibold text-slate-950 transition hover:bg-amber-400"
        >
          Add cinema
        </Link>
      </div>

      {isLoading && <p className="mt-10 text-slate-300">Loading cinemas...</p>}

      {error && (
        <div className="mt-8 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {error}
        </div>
      )}

      {!isLoading && !error && cinemas.length === 0 && (
        <div className="mt-8 rounded-xl border border-slate-700 bg-slate-800 p-6 text-slate-300">
          No cinemas have been added yet.
        </div>
      )}

      {actionError && (
        <div className="mt-8 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {actionError}
        </div>
      )}

      {!isLoading && !error && cinemas.length > 0 && (
        <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          {cinemas.map((cinema) => (
            <article
              key={cinema.id}
              className="rounded-xl border border-slate-700 bg-slate-800 p-6"
            >
              <div className="flex items-start justify-between gap-3">
                <h2 className="text-xl font-semibold">{cinema.name}</h2>

                <span
                  className={`rounded-full px-3 py-1 text-xs font-semibold ${
                    cinema.active
                      ? "bg-emerald-950 text-emerald-300"
                      : "bg-slate-700 text-slate-400"
                  }`}
                >
                  {cinema.active ? "Active" : "Inactive"}
                </span>
              </div>

              <p className="mt-3 text-slate-300">{cinema.address}</p>

              <p className="mt-1 text-sm text-slate-400">{cinema.city}</p>

              {cinema.description && (
                <p className="mt-4 text-sm text-slate-400">
                  {cinema.description}
                </p>
              )}

              <button
                type="button"
                disabled={updatingCinemaId === cinema.id}
                onClick={() => void handleStatusChange(cinema)}
                className={`mt-5 w-full rounded-lg border px-4 py-2 font-semibold transition disabled:cursor-not-allowed disabled:opacity-60 ${
                  cinema.active
                    ? "border-red-500 text-red-300 hover:bg-red-950/40"
                    : "border-emerald-500 text-emerald-300 hover:bg-emerald-950/40"
                }`}
              >
                {updatingCinemaId === cinema.id
                  ? "Updating..."
                  : cinema.active
                    ? "Deactivate"
                    : "Activate"}
              </button>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default AdminCinemasPage;
