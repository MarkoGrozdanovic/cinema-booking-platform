package com.cinemabooking.platform.exceptions;

public class PaymentAlreadySucceededException
        extends BusinessException {

    public PaymentAlreadySucceededException(String message) {
        super(message);
    }
}