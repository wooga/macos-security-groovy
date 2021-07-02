package com.wooga.security.error;

public class InvalidKeychainException extends MacOsSecurityException {
    public InvalidKeychainException(String message) {
        super(message);
    }
}
