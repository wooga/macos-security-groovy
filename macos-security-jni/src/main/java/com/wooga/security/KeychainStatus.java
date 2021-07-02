package com.wooga.security;

public class KeychainStatus {
    private enum KeychainStatusValues {
        unlocked(1),
        readable(2),
        writeable(4);

        private final int ord;

        private int getOrd() {
            return ord;
        }

        KeychainStatusValues(int ord) {
           this.ord = ord;
        }
    }

    private final int rawStatus;

    private KeychainStatus(int rawStatus) {
        this.rawStatus = rawStatus;
    }

    public static KeychainStatus from(int status) {
        return new KeychainStatus(status);
    }

    public Boolean isWriteable() {
        return (rawStatus & KeychainStatusValues.writeable.ord) > 0;
    }

    public Boolean isReadable() {
        return (rawStatus & KeychainStatusValues.readable.ord) > 0;
    }

    public Boolean isLocked() {
        return !((rawStatus & KeychainStatusValues.unlocked.ord) > 0);
    }

    public Boolean isUnlocked() {
        return !isLocked();
    }

    @Override
    public String toString() {
        return "KeychainStatus{" +
                "isWriteable=" + isWriteable() + " " +
                "isReadable=" + isReadable() + " " +
                "isLocked=" + isLocked() +
                '}';
    }
}
