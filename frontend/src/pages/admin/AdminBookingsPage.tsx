import { useEffect, useState } from "react";
import { getAdminBookings } from "../../api/adminBookingApi";
import type {
  AdminBooking,
  BookingStatus,
  PaymentStatus,
} from "../../types/adminBooking";

function getBookingStatusClasses(status: BookingStatus) {
  switch (status) {
    case "CONFIRMED":
      return "bg-emerald-950 text-emerald-300";
    case "PENDING_PAYMENT":
      return "bg-amber-950 text-amber-300";
    case "CANCELLED":
      return "bg-red-950 text-red-300";
    case "EXPIRED":
      return "bg-slate-700 text-slate-300";
  }
}

function getPaymentStatusClasses(status: PaymentStatus | null) {
  switch (status) {
    case "SUCCEEDED":
      return "bg-emerald-950 text-emerald-300";
    case "PENDING":
      return "bg-amber-950 text-amber-300";
    case "FAILED":
      return "bg-red-950 text-red-300";
    case "CANCELLED":
      return "bg-slate-700 text-slate-300";
    case null:
      return "bg-slate-800 text-slate-400";
  }
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

function AdminBookingsPage() {
  const [bookings, setBookings] = useState<AdminBooking[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadBookings() {
      try {
        const response = await getAdminBookings();

        if (!cancelled) {
          setBookings(response);
        }
      } catch {
        if (!cancelled) {
          setError("Unable to load bookings.");
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadBookings();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <section>
      <div>
        <p className="text-sm font-semibold uppercase tracking-wider text-amber-400">
          Administration
        </p>

        <h1 className="mt-2 text-4xl font-bold">Bookings</h1>

        <p className="mt-3 text-slate-400">
          Review customer bookings and their payment status.
        </p>
      </div>

      {isLoading && <p className="mt-10 text-slate-300">Loading bookings...</p>}

      {error && (
        <div className="mt-8 rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
          {error}
        </div>
      )}

      {!isLoading && !error && bookings.length === 0 && (
        <div className="mt-8 rounded-xl border border-slate-700 bg-slate-800 p-6 text-slate-300">
          No bookings have been created yet.
        </div>
      )}

      {!isLoading && !error && bookings.length > 0 && (
        <div className="mt-8 overflow-hidden rounded-xl border border-slate-700">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-700">
              <thead className="bg-slate-800">
                <tr className="text-left text-sm text-slate-300">
                  <th className="px-5 py-4 font-semibold">Reference</th>
                  <th className="px-5 py-4 font-semibold">Customer</th>
                  <th className="px-5 py-4 font-semibold">Screening</th>
                  <th className="px-5 py-4 font-semibold">Created</th>
                  <th className="px-5 py-4 font-semibold">Total</th>
                  <th className="px-5 py-4 font-semibold">Booking</th>
                  <th className="px-5 py-4 font-semibold">Payment</th>
                </tr>
              </thead>

              <tbody className="divide-y divide-slate-700 bg-slate-900">
                {bookings.map((booking) => (
                  <tr key={booking.id}>
                    <td className="whitespace-nowrap px-5 py-4 font-mono text-sm text-amber-400">
                      {booking.bookingReference}
                    </td>

                    <td className="px-5 py-4">
                      <p className="font-semibold text-white">
                        {booking.customerName}
                      </p>

                      <p className="mt-1 text-sm text-slate-400">
                        {booking.customerEmail}
                      </p>
                    </td>

                    <td className="px-5 py-4">
                      <p className="font-semibold text-white">
                        {booking.movieTitle}
                      </p>

                      <p className="mt-1 text-sm text-slate-400">
                        {booking.cinemaName} · {booking.hallName}
                      </p>

                      <p className="mt-1 whitespace-nowrap text-sm text-slate-400">
                        {formatDateTime(booking.screeningStartTime)}
                      </p>
                    </td>

                    <td className="whitespace-nowrap px-5 py-4 text-sm text-slate-300">
                      {formatDateTime(booking.createdAt)}
                    </td>

                    <td className="whitespace-nowrap px-5 py-4 font-semibold text-amber-400">
                      {booking.totalPrice.toLocaleString()} RSD
                    </td>

                    <td className="px-5 py-4">
                      <span
                        className={`whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold ${getBookingStatusClasses(
                          booking.bookingStatus,
                        )}`}
                      >
                        {booking.bookingStatus.replaceAll("_", " ")}
                      </span>
                    </td>

                    <td className="px-5 py-4">
                      <span
                        className={`whitespace-nowrap rounded-full px-3 py-1 text-xs font-semibold ${getPaymentStatusClasses(
                          booking.paymentStatus,
                        )}`}
                      >
                        {booking.paymentStatus ?? "NOT STARTED"}
                      </span>
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

export default AdminBookingsPage;
