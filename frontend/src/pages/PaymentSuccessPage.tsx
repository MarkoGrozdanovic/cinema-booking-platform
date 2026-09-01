import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router";
import { getBookingById } from "../api/bookingApi";

type ConfirmationState = "checking" | "confirmed" | "pending" | "failed";

function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();

  const [confirmationState, setConfirmationState] =
    useState<ConfirmationState>("checking");

  const bookingId = Number(searchParams.get("bookingId"));

  useEffect(() => {
    let cancelled = false;

    async function checkBookingStatus() {
      if (!Number.isInteger(bookingId) || bookingId <= 0) {
        setConfirmationState("failed");
        return;
      }

      for (let attempt = 0; attempt < 10; attempt += 1) {
        try {
          const booking = await getBookingById(bookingId);

          if (cancelled) {
            return;
          }

          if (booking.status === "CONFIRMED") {
            setConfirmationState("confirmed");
            return;
          }

          if (
            booking.status === "CANCELLED" ||
            booking.status === "EXPIRED" ||
            booking.status === "PAYMENT_FAILED"
          ) {
            setConfirmationState("failed");
            return;
          }
        } catch {
          if (!cancelled) {
            setConfirmationState("failed");
          }

          return;
        }

        await new Promise((resolve) => window.setTimeout(resolve, 1500));
      }

      if (!cancelled) {
        setConfirmationState("pending");
      }
    }

    void checkBookingStatus();

    return () => {
      cancelled = true;
    };
  }, [bookingId]);

  return (
    <section className="mx-auto max-w-xl text-center">
      <div className="rounded-xl border border-slate-700 bg-slate-800 p-8">
        {confirmationState === "checking" && (
          <>
            <h1 className="text-3xl font-bold text-amber-300">
              Confirming payment
            </h1>

            <p className="mt-4 text-slate-300">
              We are waiting for Stripe to confirm your payment securely.
            </p>

            <div className="mx-auto mt-6 h-8 w-8 animate-spin rounded-full border-4 border-slate-600 border-t-amber-400" />
          </>
        )}

        {confirmationState === "confirmed" && (
          <>
            <h1 className="text-3xl font-bold text-emerald-300">
              Payment successful
            </h1>

            <p className="mt-4 text-slate-300">
              Your booking is confirmed and your seats are reserved.
            </p>
          </>
        )}

        {confirmationState === "pending" && (
          <>
            <h1 className="text-3xl font-bold text-amber-300">
              Payment is processing
            </h1>

            <p className="mt-4 text-slate-300">
              Confirmation is taking longer than expected. Check your bookings
              again shortly.
            </p>
          </>
        )}

        {confirmationState === "failed" && (
          <>
            <h1 className="text-3xl font-bold text-red-300">
              Unable to confirm payment
            </h1>

            <p className="mt-4 text-slate-300">
              We could not confirm the booking status. Please check your
              bookings before trying another payment.
            </p>
          </>
        )}

        <Link
          to="/bookings"
          className="mt-7 inline-block rounded-lg bg-amber-500 px-5 py-3 font-semibold text-slate-950 transition hover:bg-amber-400"
        >
          View my bookings
        </Link>
      </div>
    </section>
  );
}

export default PaymentSuccessPage;
