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

package com.wooga.security;

import java.util.Objects;

public class MacOsKeychainSettings {
    boolean lockWhenSystemSleeps;
    boolean lockAfterTimeout;
    int timeout;

    static int DEFAULT_TIMEOUT = 300;

    public boolean getLockWhenSystemSleeps() {
        return lockWhenSystemSleeps;
    }

    public void setLockWhenSystemSleeps(boolean lockWhenSystemSleeps) {
        this.lockWhenSystemSleeps = lockWhenSystemSleeps;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int value) {
        timeout = value;
    }

    boolean getLockAfterTimeout() {
        return lockAfterTimeout;
    }

    void setLockAfterTimeout(boolean value) {
        if(value) {
            this.timeout = this.timeout == Integer.MAX_VALUE ? DEFAULT_TIMEOUT : this.timeout;
        } else {
            this.timeout = Integer.MAX_VALUE;
        }
    }

    public MacOsKeychainSettings(boolean lockWhenSystemSleeps) {
        this(lockWhenSystemSleeps, DEFAULT_TIMEOUT);
    }

    public MacOsKeychainSettings(boolean lockWhenSystemSleeps, boolean lockAfterTimeout) {
        this(lockWhenSystemSleeps, lockAfterTimeout ? DEFAULT_TIMEOUT : Integer.MAX_VALUE);
    }

    public MacOsKeychainSettings(boolean lockWhenSystemSleeps, int timeout) {
        this.lockWhenSystemSleeps = lockWhenSystemSleeps;
        this.timeout = timeout;
        this.lockAfterTimeout = this.timeout != Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MacOsKeychainSettings)) return false;
        MacOsKeychainSettings that = (MacOsKeychainSettings) o;
        return getLockWhenSystemSleeps() == that.getLockWhenSystemSleeps() && getLockAfterTimeout() == that.getLockAfterTimeout() && getTimeout() == that.getTimeout();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLockWhenSystemSleeps(), getLockAfterTimeout(), getTimeout());
    }
}
