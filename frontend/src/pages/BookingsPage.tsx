import axios from "axios";
import { useEffect, useState } from "react";
import { getMyBookings } from "../api/bookingApi";
import type { ApiErrorResponse } from "../types/auth";
import type { Booking, BookingStatus } from "../types/booking";
import { Link } from "react-router";

const statusClasses: Record<BookingStatus, string> = {
  PENDING_PAYMENT: "bg-amber-950 text-amber-300",
  CONFIRMED: "bg-emerald-950 text-emerald-300",
  EXPIRED: "bg-slate-800 text-slate-300",
  CANCELLED: "bg-red-950 text-red-300",
  PAYMENT_FAILED: "bg-red-950 text-red-300",
};

const dateFormatter = new Intl.DateTimeFormat("en-GB", {
  dateStyle: "medium",
  timeStyle: "short",
});

const priceFormatter = new Intl.NumberFormat("sr-RS", {
  style: "currency",
  currency: "RSD",
});

function BookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);

  const [isLoading, setIsLoading] = useState(true);

  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function loadBookings() {
      try {
        const response = await getMyBookings(controller.signal);

        setBookings(response);
      } catch (requestError) {
        if (axios.isCancel(requestError)) {
          return;
        }

        if (
          axios.isAxiosError<ApiErrorResponse>(requestError) &&
          requestError.response
        ) {
          setError(requestError.response.data.message);
        } else {
          setError("Unable to load your bookings.");
        }
      } finally {
        setIsLoading(false);
      }
    }

    void loadBookings();

    return () => controller.abort();
  }, []);

  if (isLoading) {
    return <p className="text-slate-400">Loading bookings...</p>;
  }

  if (error) {
    return (
      <div className="rounded-lg border border-red-800 bg-red-950/50 p-4 text-red-300">
        {error}
      </div>
    );
  }

  return (
    <section>
      <h1 className="text-3xl font-bold">My bookings</h1>

      <p className="mt-3 text-slate-400">
        View your current and previous cinema bookings.
      </p>

      {bookings.length === 0 ? (
        <div className="mt-8 rounded-xl border border-dashed border-slate-700 p-10 text-center text-slate-400">
          You don&apos;t have any bookings yet.
        </div>
      ) : (
        <div className="mt-8 space-y-5">
          {bookings.map((booking) => (
            <article
              className="rounded-xl border border-slate-800 bg-slate-900 p-6"
              key={booking.id}
            >
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div>
                  <h2 className="text-xl font-semibold">
                    {booking.movieTitle}
                  </h2>

                  <p className="mt-1 text-sm text-slate-400">
                    {booking.cinemaName} · {booking.hallName}
                  </p>
                </div>

                <span
                  className={`rounded-full px-3 py-1 text-xs font-semibold ${statusClasses[booking.status]}`}
                >
                  {booking.status.replaceAll("_", " ")}
                </span>
              </div>

              <dl className="mt-5 grid gap-4 text-sm sm:grid-cols-2 lg:grid-cols-4">
                <div>
                  <dt className="text-slate-500">Screening</dt>
                  <dd className="mt-1">
                    {dateFormatter.format(new Date(booking.screeningStartTime))}
                  </dd>
                </div>

                <div>
                  <dt className="text-slate-500">Seats</dt>
                  <dd className="mt-1">
                    {booking.selectedSeats
                      .map((seat) => `${seat.rowLabel}${seat.seatNumber}`)
                      .join(", ")}
                  </dd>
                </div>

                {booking.status === "PENDING_PAYMENT" && (
                  <Link
                    to={`/bookings/${booking.id}/payment`}
                    className="mt-5 inline-block rounded-lg bg-amber-500 px-4 py-2 font-semibold text-slate-950 transition hover:bg-amber-400"
                  >
                    Pay now
                  </Link>
                )}

                <div>
                  <dt className="text-slate-500">Total</dt>
                  <dd className="mt-1">
                    {priceFormatter.format(booking.totalPrice)}
                  </dd>
                </div>

                <div>
                  <dt className="text-slate-500">Reference</dt>
                  <dd className="mt-1 break-all">{booking.bookingReference}</dd>
                </div>
              </dl>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default BookingsPage;
