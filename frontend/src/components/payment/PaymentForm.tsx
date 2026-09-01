import {
  PaymentElement,
  useElements,
  useStripe,
} from "@stripe/react-stripe-js";
import { useState, type FormEvent } from "react";

interface PaymentFormProps {
  bookingId: number;
}

function PaymentForm({ bookingId }: PaymentFormProps) {
  {
    const stripe = useStripe();
    const elements = useElements();

    const [isElementReady, setIsElementReady] = useState(false);

    const [isSubmitting, setIsSubmitting] = useState(false);

    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
      event.preventDefault();

      if (!stripe || !elements || !isElementReady || isSubmitting) {
        return;
      }

      setIsSubmitting(true);
      setError(null);

      try {
        const result = await stripe.confirmPayment({
          elements,
          confirmParams: {
            return_url: `${window.location.origin}/payment/success?bookingId=${bookingId}`,
          },
        });

        if (result.error) {
          setError(result.error.message ?? "Unable to complete the payment.");
        }
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Unable to complete the payment.",
        );
      } finally {
        setIsSubmitting(false);
      }
    }

    return (
      <form onSubmit={handleSubmit}>
        <PaymentElement
          onReady={() => {
            setIsElementReady(true);
            setError(null);
          }}
          onLoadError={() => {
            setIsElementReady(false);
            setError("Stripe payment form could not be loaded.");
          }}
        />

        {!isElementReady && !error && (
          <p className="mt-4 text-sm text-slate-400">
            Loading secure payment form...
          </p>
        )}

        {error && <p className="mt-4 text-sm text-red-300">{error}</p>}

        <button
          type="submit"
          disabled={!stripe || !elements || !isElementReady || isSubmitting}
          className="mt-6 w-full rounded-lg bg-amber-500 px-4 py-3 font-semibold text-slate-950 transition hover:bg-amber-400 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isSubmitting
            ? "Processing payment..."
            : isElementReady
              ? "Pay now"
              : "Loading payment form..."}
        </button>
      </form>
    );
  }
}
export default PaymentForm;
