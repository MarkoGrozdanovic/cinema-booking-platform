import httpClient from "./httpClient";
import type {
  CreatePaymentIntentRequest,
  PaymentIntentResponse,
} from "../types/payment";
import type { PaymentStatusResponse } from "../types/paymentStatus";

export async function createPaymentIntent(
  request: CreatePaymentIntentRequest,
): Promise<PaymentIntentResponse> {
  const response = await httpClient.post<PaymentIntentResponse>(
    "/payments/payment-intents",
    request,
  );

  return response.data;
}

export async function getPaymentStatus(
  bookingId: number,
): Promise<PaymentStatusResponse> {
  const response = await httpClient.get<PaymentStatusResponse>(
    `/payments/bookings/${bookingId}/status`,
  );

  return response.data;
}
