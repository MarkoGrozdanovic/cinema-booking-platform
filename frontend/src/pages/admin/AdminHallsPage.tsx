import { isAxiosError } from "axios";
import { Link } from "react-router";
import { getAllHalls, updateHallStatus } from "../../api/hallApi";
import type { Hall } from "../../types/hall";
import { useEffect, useState } from "react";

function AdminHallsPage() {
  const [halls, setHalls] = useState<Hall[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [updatingHallId, setUpdatingHallId] = useState<number | null>(null);

  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadHalls() {
      try {
        const response = await getAllHalls();

        if (!cancelled) {
          setHalls(response);
        }
      } catch {
        if (!cancelled) {
          setError("Unable to load cinema halls.");
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadHalls();

    return () => {
      cancelled = true;
    };
  }, []);

  async function handleStatusChange(hall: Hall) {
    if (hall.active && !window.confirm(`Deactivate "${hall.name}"?`)) {
      return;
    }

    setUpdatingHallId(hall.id);
    setActionError(null);

    try {
      const updatedHall = await updateHallStatus(hall.id, !hall.active);

      setHalls((currentHalls) =>
        currentHalls.map((currentHall) =>
          currentHall.id === updatedHall.id ? updatedHall : currentHall,
        ),
      );
    } catch (error) {
      const message = isAxiosError<{ message?: string }>(error)
        ? error.response?.data.message
        : undefined;

      setActionError(message ?? "Unable to update hall status.");
    } finally {
      setUpdatingHallId(null);
    }
  }

  return (
    <section>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
            Administration
          </p>

          <h1 className="mt-2 text-4xl font-bold">Cinema halls</h1>

          <p className="mt-3 text-slate-400">
            Manage halls and their physical seat layouts.
          </p>
        </div>

        <Link
          to="/admin/halls/new"
          className="rounded-lg bg-amber-500 px-5 py-3 font-semibold text-slate-950 transition hover:bg-amber-400"
        >
          Add hall
        </Link>
      </div>

      {isLoading && (
        <p className="mt-10 text-slate-300">Loading cinema halls...</p>
      )}

      {error && (
        <div className="mt-8 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {error}
        </div>
      )}

      {!isLoading && !error && halls.length === 0 && (
        <div className="mt-8 rounded-xl border border-slate-700 bg-slate-800 p-6 text-slate-300">
          No cinema halls have been added yet.
        </div>
      )}

      {actionError && (
        <div className="mt-6 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {actionError}
        </div>
      )}

      {!isLoading && !error && halls.length > 0 && (
        <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          {halls.map((hall) => (
            <article
              key={hall.id}
              className="rounded-xl border border-slate-700 bg-slate-800 p-6"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h2 className="text-xl font-semibold">{hall.name}</h2>

                  <p className="mt-1 text-sm text-slate-400">
                    {hall.cinemaName}
                  </p>
                </div>

                <span
                  className={`rounded-full px-3 py-1 text-xs font-semibold ${
                    hall.active
                      ? "bg-emerald-950 text-emerald-300"
                      : "bg-slate-700 text-slate-400"
                  }`}
                >
                  {hall.active ? "Active" : "Inactive"}
                </span>
              </div>

              <div className="mt-5 flex items-center justify-between border-t border-slate-700 pt-4 text-sm">
                <span className="text-slate-300">
                  {hall.hallType.replaceAll("_", " ")}
                </span>

                <span className="font-semibold text-amber-400">
                  {hall.numberOfSeats} seats
                </span>
              </div>

              <button
                type="button"
                disabled={updatingHallId === hall.id}
                onClick={() => void handleStatusChange(hall)}
                className={`mt-5 w-full rounded-lg border px-4 py-2 font-semibold transition disabled:cursor-not-allowed disabled:opacity-60 ${
                  hall.active
                    ? "border-red-500 text-red-300 hover:bg-red-950/40"
                    : "border-emerald-500 text-emerald-300 hover:bg-emerald-950/40"
                }`}
              >
                {updatingHallId === hall.id
                  ? "Updating..."
                  : hall.active
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

export default AdminHallsPage;
