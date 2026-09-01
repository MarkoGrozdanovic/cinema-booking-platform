import { useEffect, useState } from "react";
import { getUpcomingScreenings } from "../api/screeningApi";
import type { Screening } from "../types/screening";
import { Link } from "react-router";

function HomePage() {
  const [screenings, setScreenings] = useState<Screening[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadScreenings() {
      try {
        const upcomingScreenings = await getUpcomingScreenings();

        setScreenings(upcomingScreenings);
      } catch {
        setError("Unable to load upcoming screenings.");
      } finally {
        setIsLoading(false);
      }
    }

    void loadScreenings();
  }, []);

  return (
    <section>
      <h1 className="text-4xl font-bold">Upcoming screenings</h1>

      <p className="mt-4 text-slate-400">
        Find a screening and reserve your seats.
      </p>

      {isLoading && (
        <p className="mt-8 text-slate-300">Loading screenings...</p>
      )}

      {error && (
        <div className="mt-8 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {error}
        </div>
      )}

      {!isLoading && !error && screenings.length === 0 && (
        <div className="mt-8 rounded-lg border border-slate-700 bg-slate-800 p-6">
          <p className="text-slate-300">There are no upcoming screenings.</p>
        </div>
      )}

      {!isLoading && !error && screenings.length > 0 && (
        <div className="mt-8 grid gap-6 md:grid-cols-2 xl:grid-cols-3">
          {screenings.map((screening) => (
            <article
              key={screening.id}
              className="rounded-xl border border-slate-700 bg-slate-800 p-6"
            >
              <h2 className="text-xl font-semibold">{screening.movieTitle}</h2>

              <p className="mt-3 text-slate-300">{screening.cinemaName}</p>

              <p className="mt-2 text-sm text-slate-400">
                {new Date(screening.startTime).toLocaleString()}
              </p>

              <div className="mt-5 flex items-center justify-between">
                <span className="font-semibold text-amber-400">
                  From {screening.basePrice.toLocaleString()} RSD
                </span>

                <span className="text-sm text-slate-400">
                  {screening.numberOfSeats} seats
                </span>
              </div>

              <Link
                to={`/screenings/${screening.id}`}
                className="mt-6 block rounded-lg bg-amber-500 px-4 py-2 text-center font-semibold text-slate-950 transition hover:bg-amber-400"
              >
                Select seats
              </Link>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default HomePage;
