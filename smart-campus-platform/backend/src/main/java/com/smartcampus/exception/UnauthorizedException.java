package com.smartcampus.exception;

/** Thrown when an authenticated user tries to touch data that is not theirs. Mapped to HTTP 403. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
