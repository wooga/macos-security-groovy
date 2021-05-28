package com.wooga.security;

import java.io.File;
import java.util.Objects;

class DefaultKeychainRef implements KeychainRef {
    private final long ref;
    private File path;
    private Boolean isReleased;

    public File getPath() {
        if(path == null) {
            path = KeychainRef.path(this);
        }
        return path;
    }

    public long getRef() {
        return ref;
    }

    public boolean exists() {
        return getPath().exists();
    }

    private DefaultKeychainRef(long ref) {
        this.ref = ref;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(!isReleased) {
            MacOsSecurity.release(this.ref);
        }
        isReleased = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeychainRef)) return false;
        KeychainRef that = (KeychainRef) o;
        return ref == that.getRef();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref);
    }

    @Override
    public String toString() {
        return "KeychainRef{" +
                ref +
                '}';
    }
}
