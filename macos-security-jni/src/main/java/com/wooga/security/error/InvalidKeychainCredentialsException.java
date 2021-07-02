package com.wooga.security.error;

public class InvalidKeychainCredentialsException extends MacOsSecurityException {
    public InvalidKeychainCredentialsException(String message) {
        super(message);
    }
}
