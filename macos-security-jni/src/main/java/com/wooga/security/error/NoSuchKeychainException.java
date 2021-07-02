package com.wooga.security.error;

public class NoSuchKeychainException extends MacOsSecurityException {
    public NoSuchKeychainException(String message) {
        super(message);
    }
}
