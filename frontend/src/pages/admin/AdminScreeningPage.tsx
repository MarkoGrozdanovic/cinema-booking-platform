import { useEffect, useState } from "react";
import { Link } from "react-router";
import { cancelScreening, getAdminScreenings } from "../../api/screeningApi";
import type { Screening } from "../../types/screening";
import { isAxiosError } from "axios";

function AdminScreeningsPage() {
  const [screenings, setScreenings] = useState<Screening[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [updatingScreeningId, setUpdatingScreeningId] = useState<number | null>(
    null,
  );

  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadScreenings() {
      try {
        const response = await getAdminScreenings();

        if (!cancelled) {
          setScreenings(response);
        }
      } catch {
        if (!cancelled) {
          setError("Unable to load screenings.");
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadScreenings();

    return () => {
      cancelled = true;
    };
  }, []);

  function formatDateTime(value: string) {
    return new Intl.DateTimeFormat("en-GB", {
      dateStyle: "medium",
      timeStyle: "short",
    }).format(new Date(value));
  }

  async function handleCancellation(screening: Screening) {
    const confirmed = window.confirm(
      `Cancel the screening for "${screening.movieTitle}"?`,
    );

    if (!confirmed) {
      return;
    }

    setUpdatingScreeningId(screening.id);
    setActionError(null);

    try {
      const updatedScreening = await cancelScreening(screening.id);

      setScreenings((currentScreenings) =>
        currentScreenings.map((currentScreening) =>
          currentScreening.id === updatedScreening.id
            ? updatedScreening
            : currentScreening,
        ),
      );
    } catch (error) {
      const message = isAxiosError<{ message?: string }>(error)
        ? error.response?.data.message
        : undefined;

      setActionError(message ?? "Unable to cancel screening.");
    } finally {
      setUpdatingScreeningId(null);
    }
  }

  return (
    <section>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
            Administration
          </p>

          <h1 className="mt-2 text-4xl font-bold">Screenings</h1>

          <p className="mt-3 text-slate-400">
            Review and manage scheduled movie screenings.
          </p>
        </div>

        <Link
          to="/admin/screenings/new"
          className="rounded-lg bg-amber-500 px-5 py-3 font-semibold text-slate-950 transition hover:bg-amber-400"
        >
          Add screening
        </Link>
      </div>

      {isLoading && (
        <p className="mt-10 text-slate-300">Loading screenings...</p>
      )}

      {error && (
        <div className="mt-8 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {error}
        </div>
      )}

      {!isLoading && !error && screenings.length === 0 && (
        <div className="mt-8 rounded-xl border border-slate-700 bg-slate-800 p-6 text-slate-300">
          No screenings have been added yet.
        </div>
      )}

      {actionError && (
        <div className="mt-6 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {actionError}
        </div>
      )}

      {!isLoading && !error && screenings.length > 0 && (
        <div className="mt-8 overflow-hidden rounded-xl border border-slate-700">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-700">
              <thead className="bg-slate-800">
                <tr className="text-left text-sm text-slate-300">
                  <th className="px-5 py-4 font-semibold">Movie</th>
                  <th className="px-5 py-4 font-semibold">Cinema</th>
                  <th className="px-5 py-4 font-semibold">Start time</th>
                  <th className="px-5 py-4 font-semibold">Price</th>
                  <th className="px-5 py-4 font-semibold">Seats</th>
                  <th className="px-5 py-4 font-semibold">Status</th>
                  <th className="px-6 py-4 text-right">Actions</th>
                </tr>
              </thead>

              <tbody className="divide-y divide-slate-700 bg-slate-900">
                {screenings.map((screening) => (
                  <tr key={screening.id}>
                    <td className="px-5 py-4 font-semibold text-white">
                      {screening.movieTitle}
                    </td>

                    <td className="px-5 py-4 text-slate-300">
                      {screening.cinemaName}
                    </td>

                    <td className="whitespace-nowrap px-5 py-4 text-slate-300">
                      {formatDateTime(screening.startTime)}
                    </td>

                    <td className="whitespace-nowrap px-5 py-4 text-amber-400">
                      {screening.basePrice.toLocaleString()} RSD
                    </td>

                    <td className="px-5 py-4 text-slate-300">
                      {screening.numberOfSeats}
                    </td>

                    <td className="px-5 py-4">
                      <span
                        className={`rounded-full px-3 py-1 text-xs font-semibold ${
                          screening.status === "SCHEDULED"
                            ? "bg-emerald-950 text-emerald-300"
                            : screening.status === "CANCELLED"
                              ? "bg-red-950 text-red-300"
                              : "bg-slate-700 text-slate-300"
                        }`}
                      >
                        {screening.status}
                      </span>
                    </td>

                    <td className="px-6 py-4 text-right">
                      {screening.status === "SCHEDULED" ? (
                        <button
                          type="button"
                          disabled={updatingScreeningId === screening.id}
                          onClick={() => void handleCancellation(screening)}
                          className="rounded-lg border border-red-500 px-4 py-2 text-sm font-semibold text-red-300 transition hover:bg-red-950/40 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                          {updatingScreeningId === screening.id
                            ? "Cancelling..."
                            : "Cancel"}
                        </button>
                      ) : (
                        <span className="text-sm text-slate-500">—</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </section>
  );
}

export default AdminScreeningsPage;
