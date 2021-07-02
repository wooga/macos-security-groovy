/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wooga.security

import com.wooga.security.command.*
import com.wooga.security.error.DuplicateKeychainException
import com.wooga.security.error.InvalidKeychainException
import com.wooga.security.error.NoSuchKeychainException
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

class MacOsKeychain implements KeychainRef {

    final String password
    final KeychainRef ref

    @Override
    File getPath() {
        ref.getPath()
    }

    @Override
    long getRef() {
        ref.ref
    }

    private MacOsKeychain(KeychainRef ref, String password) {
        this.password = password
        this.ref = ref
    }

    static MacOsKeychain create(File location, String password) throws DuplicateKeychainException {
        KeychainRef ref = KeychainRef.create(location, password);
        return new MacOsKeychain(ref, password);
    }

    static MacOsKeychain create(File location, String password, MacOsKeychainSettings initialSettings) throws DuplicateKeychainException {
        KeychainRef ref = KeychainRef.create(location, password, initialSettings);
        return new MacOsKeychain(ref, password);
    }

    static MacOsKeychain open(File location) {
        return open(location, null);
    }

    static MacOsKeychain open(File location, String password) {
        KeychainRef ref = KeychainRef.open(location);
        return new MacOsKeychain(ref, password);
    }

    static MacOsKeychain open(KeychainRef ref) {
        return new MacOsKeychain(ref, null);
    }

    File getLocation() {
        path
    }

    KeychainStatus getStatus() throws NoSuchKeychainException, InvalidKeychainException {
        status(this)
    }

    Boolean unlock() {
        unlock(this, password).isUnlocked()
    }

    Boolean lock() {
        lock(this).isLocked()
    }

    Boolean getLockWhenSystemSleeps() {
        getSettings().lockWhenSystemSleeps
    }

    Boolean setLockWhenSystemSleeps(Boolean value) {
        withSettings {
            it.lockWhenSystemSleeps = value
        }
    }

    Integer getTimeout() {
        getSettings().timeout
    }

    Boolean setTimeout(Integer timeout) {
        withSettings {
            it.timeout = timeout
        }
    }

    Boolean getLockAfterTimeout() {
        getSettings().lockAfterTimeout
    }

    Boolean withSettings(@ClosureParams(value = FromString.class, options = ["com.wooga.security.MacOsKeychainSettings"]) Closure action) {
        def settings = getSettings()

        action.call(settings)
        setSettings(settings)
    }

    MacOsKeychainSettings getSettings() {
        getSettings(this)
    }

    Boolean setSettings(MacOsKeychainSettings settings) {
        setSettings(this, settings)
    }

    Boolean addGenericPassword(String account, String service, String password, Map config = [:]) {
        new AddGenericPassword(account, service, password, config)
                .withKeychain(this)
                .execute()
        true
    }

    Boolean addInternetPassword(String account, String server, String password, Map config = [:]) {
        new AddInternetPassword(account, server, password, config)
                .withKeychain(this)
                .execute()
        true
    }

    Boolean importFile(File importFile, Map config = [:]) {
        new Import(importFile, this.location, config).execute()
        true
    }

    String findCertificate(Map query = [:]) {
        new FindCertificate(query).withKeychain(this).execute()
    }

    String findKey(Map query = [:]) {
        new FindKey(query).withKeychain(this).execute()
    }

    String findIdentity(Map query = [:]) {
        new FindIdentity(query).withKeychain(this).execute()
    }

    String findGenericPassword(String account, String service, Map query = [:]) {
        new FindGenericPassword(account, service, query).withKeychain(this).printPasswordOnly().execute()
    }

    String findInternetPassword(String account, String server, Map query = [:]) {
        new FindInternetPassword(account, server, query).withKeychain(this).printPasswordOnly().execute()
    }

    Boolean delete() {
        delete(this)
    }

    boolean exists() {
        this.ref.exists()
    }

    @Override
    String toString() {
        return "MacOsKeychain{" +
                "ref=" + ref +
                "location=" + location +
                '}';
    }

    @Override
    boolean equals(Object o) {
        if (this == o) return true
        if (!(o instanceof MacOsKeychain)) return false
        MacOsKeychain that = (MacOsKeychain) o
        return getRef() == that.ref;
    }

    @Override
    int hashCode() {
        return Objects.hash(getRef())
    }
}
