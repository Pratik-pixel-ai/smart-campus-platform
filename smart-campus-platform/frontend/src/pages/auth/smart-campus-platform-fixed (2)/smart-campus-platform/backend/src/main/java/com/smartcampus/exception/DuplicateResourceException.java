package com.smartcampus.exception;

/** Thrown when a unique field (email, roll number, subject code) already exists. Mapped to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
