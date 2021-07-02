package com.wooga.security.error;

public class NoDefaultKeychainException extends MacOsSecurityException {
    public NoDefaultKeychainException(String message) {
        super(message);
    }
}
