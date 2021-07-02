package com.wooga.security;

import com.wooga.security.error.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface KeychainRef {
    File getPath();

    long getRef();

    boolean exists();

    static KeychainRef open(String path) throws IOException {
        return open(new File(path));
    }

    static KeychainRef open(File path) throws IOException {
        return MacOsSecurity.keychainOpen(canonical(path).getAbsolutePath());
    }

    static KeychainRef create(File path, String password) throws IOException, DuplicateKeychainException {
        return MacOsSecurity.keychainCreate(canonical(path).getAbsolutePath(), password);
    }

    static KeychainRef create(File path, String password, MacOsKeychainSettings initialSettings) throws IOException, DuplicateKeychainException {
        return MacOsSecurity.keychainCreate(canonical(path).getAbsolutePath(), password, initialSettings);
    }

    static File path(KeychainRef keychain) {
        return new File(MacOsSecurity.keychainGetPath(keychain));
    }

    static KeychainRef defaultKeychain() {
        return MacOsSecurity.keychainCopyDefault();
    }

    static KeychainRef defaultKeychain(Domain domain) {
        return MacOsSecurity.keychainCopyDefault(domain.ordinal());
    }

    static KeychainStatus status(KeychainRef keychain) throws NoSuchKeychainException, InvalidKeychainException {
        return KeychainStatus.from(MacOsSecurity.keychainGetStatus(keychain));
    }

    static KeychainStatus lock(KeychainRef keychain) throws NoSuchKeychainException, InvalidKeychainException {
        return KeychainStatus.from(MacOsSecurity.keychainLock(keychain));
    }

    static Boolean lockAll() {
        return MacOsSecurity.keychainLockAll();
    }

    static KeychainStatus unlock(KeychainRef keychain, String password) throws NoSuchKeychainException, InvalidKeychainException, InvalidKeychainCredentialsException {
        return KeychainStatus.from(MacOsSecurity.keychainUnlock(keychain, password));
    }

    static boolean delete(KeychainRef keychain) throws MacOsSecurityException {
        return MacOsSecurity.keychainDelete(keychain);
    }

    static MacOsKeychainSettings getSettings(KeychainRef keychain) throws NoSuchKeychainException, InvalidKeychainException {
        return MacOsSecurity.keychainCopySettings(keychain);
    }

    static boolean setSettings(KeychainRef keychain, MacOsKeychainSettings settings) throws NoSuchKeychainException, InvalidKeychainException {
        return MacOsSecurity.keychainSetSettings(keychain, settings);
    }

    static List<KeychainRef> listKeychains() {
        KeychainRef[] array = MacOsSecurity.keychainCopySearchList();
        return new ArrayList<>(Arrays.asList(array));
    }

    static List<KeychainRef> listKeychains(Domain domain) {
        KeychainRef[] array = new KeychainRef[0];
        try {
            array = MacOsSecurity.keychainCopySearchList(domain.ordinal());
        } catch (InvalidPreferenceDomainException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    static void setKeychainList(Collection<KeychainRef> keychains) {
        KeychainRef[] keychainRefs = keychains.toArray(new KeychainRef[0]);
        MacOsSecurity.keychainSetSearchList(keychainRefs);
    }

    static void setKeychainList(Domain domain, Collection<KeychainRef> keychains) {
        KeychainRef[] keychainRefs = keychains.toArray(new KeychainRef[0]);
        try {
            MacOsSecurity.keychainSetSearchList(domain.ordinal(), keychainRefs);
        } catch (InvalidPreferenceDomainException e) {
            e.printStackTrace();
        }
    }

    static String expandPath(String path) {
        if (path.startsWith("~" + File.separator)) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    static File expandPath(File path) {
        return new File(expandPath(path.getPath()));
    }

    static File canonical(File keychain) {
        try {
            return expandPath(keychain).getCanonicalFile();
        } catch (IOException ignored) {
            return keychain;
        }
    }
}
