package com.cinemabooking.platform.exceptions;

public class InactiveMovieException extends BusinessException {
    public InactiveMovieException(String message) {
        super(message);
    }
}
