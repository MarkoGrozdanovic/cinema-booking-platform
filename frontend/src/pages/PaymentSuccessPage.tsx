import { Link } from "react-router";

function PaymentSuccessPage() {
  return (
    <section className="mx-auto max-w-xl text-center">
      <div className="rounded-xl border border-emerald-500/40 bg-emerald-950/30 p-8">
        <h1 className="text-3xl font-bold text-emerald-300">
          Payment submitted
        </h1>

        <p className="mt-4 text-slate-300">
          Stripe has submitted your payment. Your booking will be confirmed
          after the backend processes the signed webhook.
        </p>

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
