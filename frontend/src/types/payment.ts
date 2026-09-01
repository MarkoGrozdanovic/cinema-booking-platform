export interface CreatePaymentIntentRequest {
  bookingId: number;
}

export type PaymentStatus = "PENDING" | "SUCCEEDED" | "FAILED" | "CANCELLED";

export interface PaymentIntentResponse {
  paymentId: number;
  bookingId: number;
  providerPaymentId: string;
  clientSecret: string;
  status: PaymentStatus;
  amount: number;
  currency: string;
}
