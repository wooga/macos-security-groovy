package com.wooga.security.error;

public class DuplicateKeychainException extends MacOsSecurityException {
    public DuplicateKeychainException(String message) {
        super(message);
    }
}
