package com.example.authentification.Services;

public class UserAlreadyExistsException extends RuntimeException {
    // Constructeurs
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}