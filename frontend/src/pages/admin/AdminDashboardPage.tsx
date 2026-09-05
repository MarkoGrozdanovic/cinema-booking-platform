import { Link } from "react-router";

function AdminDashboardPage() {
  return (
    <section>
      <div>
        <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
          Administration
        </p>

        <h1 className="mt-2 text-4xl font-bold">Admin dashboard</h1>

        <p className="mt-4 text-slate-400">
          Manage cinema screenings and platform data.
        </p>
      </div>

      <div className="mt-10 grid gap-6 md:grid-cols-2 xl:grid-cols-4">
        <article className="rounded-xl border border-slate-700 bg-slate-800 p-6">
          <h2 className="text-xl font-semibold">Screenings</h2>

          <p className="mt-3 text-sm text-slate-400">
            Schedule new movie screenings and configure ticket prices.
          </p>

          <Link
            to="/admin/screenings/new"
            className="mt-5 inline-block rounded-lg bg-amber-500 px-4 py-2 font-semibold text-slate-950 transition hover:bg-amber-400"
          >
            Create screening
          </Link>
        </article>

        <article className="rounded-xl border border-slate-700 bg-slate-800 p-6">
          <h2 className="text-xl font-semibold">Movies</h2>

          <p className="mt-3 text-sm text-slate-400">
            Movie administration will be added after the screening workflow.
          </p>

          <Link
            to="/admin/movies"
            className="mt-5 inline-block rounded-lg border border-amber-500 px-4 py-2 font-semibold text-amber-300 transition hover:bg-amber-950/30"
          >
            Manage movies
          </Link>
        </article>

        <article className="rounded-xl border border-slate-700 bg-slate-800 p-6">
          <h2 className="text-xl font-semibold">Cinemas</h2>

          <p className="mt-3 text-sm text-slate-400">
            Cinema, hall, and seat administration will be added in a later step.
          </p>

          <div className="mt-5 flex flex-wrap gap-3">
            <Link
              to="/admin/cinemas"
              className="rounded-lg border border-amber-500 px-4 py-2 font-semibold text-amber-300 transition hover:bg-amber-950/30"
            >
              Manage cinemas
            </Link>

            <Link
              to="/admin/halls"
              className="rounded-lg border border-slate-500 px-4 py-2 font-semibold text-slate-300 transition hover:bg-slate-700"
            >
              Manage halls
            </Link>
          </div>
        </article>

        <article className="rounded-xl border border-slate-700 bg-slate-800 p-6">
          <h2 className="text-xl font-semibold">Bookings</h2>

          <p className="mt-3 text-sm text-slate-400">
            Review customer bookings and monitor their payment status.
          </p>

          <Link
            to="/admin/bookings"
            className="mt-5 inline-block rounded-lg border border-amber-500 px-4 py-2 font-semibold text-amber-300 transition hover:bg-amber-950/30"
          >
            View bookings
          </Link>
        </article>
      </div>
    </section>
  );
}

export default AdminDashboardPage;
