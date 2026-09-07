import { Link, useSearchParams } from "react-router";

function PaymentFailedPage() {
  const [searchParams] = useSearchParams();
  const bookingId = Number(searchParams.get("bookingId"));

  const validBookingId = Number.isInteger(bookingId) && bookingId > 0;

  return (
    <section className="mx-auto max-w-xl text-center">
      <div className="rounded-xl border border-red-500/30 bg-slate-800 p-8">
        <h1 className="text-3xl font-bold text-red-300">
          Payment was not completed
        </h1>

        <p className="mt-4 text-slate-300">
          Your payment failed or was cancelled. You can try again while your
          booking is still active.
        </p>

        <div className="mt-7 flex flex-wrap justify-center gap-3">
          {validBookingId && (
            <Link
              to={`/bookings/${bookingId}/payment`}
              className="rounded-lg bg-amber-500 px-5 py-3 font-semibold text-slate-950 transition hover:bg-amber-400"
            >
              Try payment again
            </Link>
          )}

          <Link
            to="/bookings"
            className="rounded-lg border border-slate-600 px-5 py-3 font-semibold text-slate-200 transition hover:bg-slate-700"
          >
            View my bookings
          </Link>
        </div>
      </div>
    </section>
  );
}

export default PaymentFailedPage;
