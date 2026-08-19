package com.cinemabooking.platform.exceptions;

public class SeatNotAvailableException extends BusinessException {
    public SeatNotAvailableException(String message) {
        super(message);
    }
}
