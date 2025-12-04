package com.booking.api.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}