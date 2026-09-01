import { Elements } from "@stripe/react-stripe-js";
import { loadStripe } from "@stripe/stripe-js";
import { isAxiosError } from "axios";
import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router";
import { createPaymentIntent } from "../api/paymentApi";
import PaymentForm from "../components/payment/PaymentForm";

const publishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;

const stripePromise = publishableKey ? loadStripe(publishableKey) : null;

function PaymentPage() {
  const { bookingId } = useParams<{
    bookingId: string;
  }>();

  const hasRequestedPaymentIntent = useRef(false);

  const [clientSecret, setClientSecret] = useState<string | null>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadPaymentIntent() {
      if (hasRequestedPaymentIntent.current) {
        return;
      }

      hasRequestedPaymentIntent.current = true;

      const id = Number(bookingId);

      if (!Number.isInteger(id) || id <= 0) {
        setError("Invalid booking ID.");
        setIsLoading(false);
        return;
      }

      try {
        const paymentIntent = await createPaymentIntent({
          bookingId: id,
        });

        setClientSecret(paymentIntent.clientSecret);
      } catch (error) {
        if (isAxiosError<{ message?: string }>(error)) {
          setError(
            error.response?.data.message ?? "Unable to initialize payment.",
          );
        } else {
          setError("Unable to initialize payment.");
        }
      } finally {
        setIsLoading(false);
      }
    }

    void loadPaymentIntent();
  }, [bookingId]);

  if (!publishableKey) {
    return (
      <div className="rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
        Stripe publishable key is missing.
      </div>
    );
  }

  if (isLoading) {
    return <p className="text-slate-300">Preparing secure payment...</p>;
  }

  if (error || !clientSecret) {
    return (
      <div className="rounded-lg border border-red-500/40 bg-red-950/40 p-4 text-red-200">
        {error ?? "Unable to initialize payment."}
      </div>
    );
  }

  return (
    <section className="mx-auto max-w-xl">
      <h1 className="text-3xl font-bold">Complete payment</h1>

      <p className="mt-3 text-slate-400">
        Your payment is processed securely by Stripe.
      </p>

      <div className="mt-8 rounded-xl border border-slate-700 bg-slate-800 p-6">
        <Elements
          stripe={stripePromise}
          options={{
            clientSecret,
            appearance: {
              theme: "night",
            },
          }}
        >
          <PaymentForm bookingId={Number(bookingId)} />
        </Elements>
      </div>
    </section>
  );
}

export default PaymentPage;
