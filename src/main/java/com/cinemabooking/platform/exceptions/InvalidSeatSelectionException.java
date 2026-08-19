package com.cinemabooking.platform.exceptions;

public class InvalidSeatSelectionException extends RuntimeException{
    public InvalidSeatSelectionException(String message){
        super(message);
    }
}
