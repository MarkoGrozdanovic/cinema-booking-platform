import { useEffect, useState } from "react";
import { getAdminBookings } from "../../api/adminBookingApi";
import type {
  AdminBooking,
  BookingStatus,
  PaymentStatus,
} from "../../types/adminBooking";

type BookingStatusFilter = "ALL" | BookingStatus;
type PaymentStatusFilter = "ALL" | "NOT_STARTED" | PaymentStatus;

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

  const [searchTerm, setSearchTerm] = useState("");
  const [bookingStatusFilter, setBookingStatusFilter] =
    useState<BookingStatusFilter>("ALL");
  const [paymentStatusFilter, setPaymentStatusFilter] =
    useState<PaymentStatusFilter>("ALL");

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

  const normalizedSearch = searchTerm.trim().toLowerCase();

  const filteredBookings = bookings.filter((booking) => {
    const matchesSearch =
      normalizedSearch.length === 0 ||
      booking.bookingReference.toLowerCase().includes(normalizedSearch) ||
      booking.customerName.toLowerCase().includes(normalizedSearch) ||
      booking.customerEmail.toLowerCase().includes(normalizedSearch) ||
      booking.movieTitle.toLowerCase().includes(normalizedSearch) ||
      booking.cinemaName.toLowerCase().includes(normalizedSearch) ||
      booking.hallName.toLowerCase().includes(normalizedSearch);

    const matchesBookingStatus =
      bookingStatusFilter === "ALL" ||
      booking.bookingStatus === bookingStatusFilter;

    const matchesPaymentStatus =
      paymentStatusFilter === "ALL" ||
      (paymentStatusFilter === "NOT_STARTED"
        ? booking.paymentStatus === null
        : booking.paymentStatus === paymentStatusFilter);

    return matchesSearch && matchesBookingStatus && matchesPaymentStatus;
  });

  const hasActiveFilters =
    searchTerm.length > 0 ||
    bookingStatusFilter !== "ALL" ||
    paymentStatusFilter !== "ALL";

  function clearFilters() {
    setSearchTerm("");
    setBookingStatusFilter("ALL");
    setPaymentStatusFilter("ALL");
  }

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
        <>
          <div className="mt-8 rounded-xl border border-slate-700 bg-slate-800 p-5">
            <div className="grid gap-4 md:grid-cols-3">
              <div>
                <label
                  htmlFor="booking-search"
                  className="mb-2 block text-sm font-semibold text-slate-300"
                >
                  Search
                </label>

                <input
                  id="booking-search"
                  type="search"
                  value={searchTerm}
                  onChange={(event) => setSearchTerm(event.target.value)}
                  placeholder="Reference, customer, movie..."
                  className="w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-2.5 text-white outline-none transition placeholder:text-slate-500 focus:border-amber-500"
                />
              </div>

              <div>
                <label
                  htmlFor="booking-status-filter"
                  className="mb-2 block text-sm font-semibold text-slate-300"
                >
                  Booking status
                </label>

                <select
                  id="booking-status-filter"
                  value={bookingStatusFilter}
                  onChange={(event) =>
                    setBookingStatusFilter(
                      event.target.value as BookingStatusFilter,
                    )
                  }
                  className="w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-2.5 text-white outline-none transition focus:border-amber-500"
                >
                  <option value="ALL">All booking statuses</option>
                  <option value="PENDING_PAYMENT">Pending payment</option>
                  <option value="CONFIRMED">Confirmed</option>
                  <option value="CANCELLED">Cancelled</option>
                  <option value="EXPIRED">Expired</option>
                </select>
              </div>

              <div>
                <label
                  htmlFor="payment-status-filter"
                  className="mb-2 block text-sm font-semibold text-slate-300"
                >
                  Payment status
                </label>

                <select
                  id="payment-status-filter"
                  value={paymentStatusFilter}
                  onChange={(event) =>
                    setPaymentStatusFilter(
                      event.target.value as PaymentStatusFilter,
                    )
                  }
                  className="w-full rounded-lg border border-slate-600 bg-slate-900 px-4 py-2.5 text-white outline-none transition focus:border-amber-500"
                >
                  <option value="ALL">All payment statuses</option>
                  <option value="NOT_STARTED">Not started</option>
                  <option value="PENDING">Pending</option>
                  <option value="SUCCEEDED">Succeeded</option>
                  <option value="FAILED">Failed</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>
            </div>

            <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-slate-700 pt-4">
              <p className="text-sm text-slate-400">
                Showing{" "}
                <span className="font-semibold text-white">
                  {filteredBookings.length}
                </span>{" "}
                of{" "}
                <span className="font-semibold text-white">
                  {bookings.length}
                </span>{" "}
                bookings
              </p>

              {hasActiveFilters && (
                <button
                  type="button"
                  onClick={clearFilters}
                  className="rounded-lg border border-slate-600 px-4 py-2 text-sm font-semibold text-slate-300 transition hover:border-amber-500 hover:text-amber-300"
                >
                  Clear filters
                </button>
              )}
            </div>
          </div>

          {filteredBookings.length === 0 ? (
            <div className="mt-6 rounded-xl border border-slate-700 bg-slate-800 p-6 text-slate-300">
              <p>No bookings match the selected filters.</p>

              <button
                type="button"
                onClick={clearFilters}
                className="mt-4 rounded-lg border border-amber-500 px-4 py-2 font-semibold text-amber-300 transition hover:bg-amber-950/30"
              >
                Clear filters
              </button>
            </div>
          ) : (
            <div className="mt-6 overflow-hidden rounded-xl border border-slate-700">
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
                    {filteredBookings.map((booking) => (
                      <tr
                        key={booking.id}
                        className="transition hover:bg-slate-800/60"
                      >
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
                            {booking.paymentStatus?.replaceAll("_", " ") ??
                              "NOT STARTED"}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
    </section>
  );
}

export default AdminBookingsPage;
