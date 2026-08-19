package com.cinemabooking.platform.exceptions;

public class HallHasNoActiveSeatsException extends RuntimeException {
    public HallHasNoActiveSeatsException(String message) {
        super(message);
    }
}
