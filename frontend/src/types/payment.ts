export interface CreatePaymentIntentRequest {
  bookingId: number;
}

export interface PaymentIntentResponse {
  clientSecret: string;
}
