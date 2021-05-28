package com.wooga.security;

import com.wooga.security.error.*;

class MacOsSecurity {
    static {
        NativeLoader.loadLibrary(MacOsSecurity.class.getClassLoader(), "macos-security-jni", "com/wooga/security/" );
    }

    public static native KeychainRef keychainCreate(String path, String password) throws DuplicateKeychainException;
    public static native KeychainRef keychainCreate(String path, String password, MacOsKeychainSettings initialSettings) throws DuplicateKeychainException;

    public static native boolean keychainDelete(KeychainRef keychainRef) throws MacOsSecurityException;

    public static native int keychainLock(KeychainRef keychainRef) throws NoSuchKeychainException, InvalidKeychainException;
    public static native boolean keychainLockAll();
    public static native int keychainUnlock(KeychainRef keychainRef, String password) throws NoSuchKeychainException, InvalidKeychainException, InvalidKeychainCredentialsException;

    public static native MacOsKeychainSettings keychainCopySettings(KeychainRef keychainRef) throws NoSuchKeychainException, InvalidKeychainException;
    public static native boolean keychainSetSettings(KeychainRef keychainRef, MacOsKeychainSettings settings) throws NoSuchKeychainException, InvalidKeychainException;

    public static native KeychainRef keychainOpen(String path);

    public static native boolean keychainSetDefault(KeychainRef keychainRef) throws NoSuchKeychainException, InvalidKeychainException;
    public static native boolean keychainSetDefault(int preferenceDomain, KeychainRef keychainRef) throws NoSuchKeychainException, InvalidKeychainException;

    public static native KeychainRef keychainCopyDefault();
    public static native KeychainRef keychainCopyDefault(int preferenceDomain);

    public static native String keychainGetPath(KeychainRef keychainRef);
    public static native int keychainGetStatus(KeychainRef keychainRef) throws NoSuchKeychainException, InvalidKeychainException;

    public static native boolean keychainSetSearchList(KeychainRef[] keychains);
    public static native boolean keychainSetSearchList(int preferenceDomain, KeychainRef[] keychains) throws InvalidPreferenceDomainException;

    public static native KeychainRef[] keychainCopySearchList();
    public static native KeychainRef[] keychainCopySearchList(int preferenceDomain) throws InvalidPreferenceDomainException;

    public static native int keychainGetPreferenceDomain();
    public static native boolean keychainSetPreferenceDomain(int preferenceDomain) throws InvalidPreferenceDomainException;

    public static native void release(long keychainRef);
}
