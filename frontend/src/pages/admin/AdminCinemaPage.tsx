import { useEffect, useState } from "react";
import { Link } from "react-router";
import { getAllCinemas } from "../../api/cinemaApi";
import type { Cinema } from "../../types/cinema";

function AdminCinemasPage() {
  const [cinemas, setCinemas] = useState<Cinema[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default AdminCinemasPage;
