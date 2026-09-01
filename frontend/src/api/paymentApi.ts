import httpClient from "./httpClient";
import type {
  CreatePaymentIntentRequest,
  PaymentIntentResponse,
} from "../types/payment";

export async function createPaymentIntent(
  request: CreatePaymentIntentRequest,
): Promise<PaymentIntentResponse> {
  const response = await httpClient.post<PaymentIntentResponse>(
    "/payments/payment-intents",
    request,
  );

  return response.data;
}
