/*
 * Copyright 2018-2020 Wooga GmbH
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

class MacOsKeychainSearchList implements List<KeychainRef>, Set<KeychainRef> {

    private class KeychainSearchListIterator implements Iterator<KeychainRef> {
        private Iterator<KeychainRef> innerIterator
        private KeychainRef current

        KeychainSearchListIterator(Iterator<KeychainRef> innerIterator) {
            this.innerIterator = innerIterator
        }

        @Override
        boolean hasNext() {
            return innerIterator.hasNext()
        }

        @Override
        KeychainRef next() {
            current = innerIterator.next()
            return current
        }

        @Override
        void remove() {
            remove(current)
        }
    }

    private class KeychainLookupListIterator implements ListIterator<KeychainRef> {
        private ListIterator<KeychainRef> innerIterator
        private KeychainRef current

        KeychainLookupListIterator(ListIterator<KeychainRef> innerIterator) {
            this.innerIterator = innerIterator
        }

        @Override
        boolean hasNext() {
            return innerIterator.hasNext()
        }

        @Override
        KeychainRef next() {
            current = innerIterator.next()
            return current
        }

        @Override
        void remove() {
            remove(current)
        }

        @Override
        boolean hasPrevious() {
            return innerIterator.hasPrevious()
        }

        @Override
        KeychainRef previous() {
            current = innerIterator.previous()
            return current
        }

        @Override
        int nextIndex() {
            return innerIterator.nextIndex()
        }

        @Override
        int previousIndex() {
            return innerIterator.previousIndex()
        }

        @Override
        void set(KeychainRef file) {
            throw new UnsupportedOperationException()
        }

        @Override
        void add(KeychainRef file) {
            throw new UnsupportedOperationException()
        }
    }

    private final Domain domain

    MacOsKeychainSearchList(Domain domain = Domain.user) {
        this.domain = domain
    }

    private static List<KeychainRef> listKeychains(Domain domain) {
        KeychainRef.listKeychains(domain)
    }

    private static void setKeychains(List<KeychainRef> keychains, Domain domain) {
        KeychainRef.setKeychainList(domain, keychains)
    }

    @Override
    int size() {
        listKeychains(domain).size()
    }

    @Override
    boolean isEmpty() {
        size() == 0
    }

    @Override
    boolean contains(Object o) {
        Objects.requireNonNull(o)

        if (!KeychainRef.isInstance(o)) {
            throw new ClassCastException("expect object of type ${KeychainRef.name}")
        }

        KeychainRef keychain = o as KeychainRef
        listKeychains(domain).contains(keychain)
    }

    @Override
    Iterator<KeychainRef> iterator() {
        new KeychainSearchListIterator(listKeychains(domain).iterator())
    }

    @Override
    Object[] toArray() {
        listKeychains(domain).toArray()
    }

    @Override
    def <T> T[] toArray(T[] a) {
        listKeychains(domain).toArray(a)
    }

    @Override
    boolean add(KeychainRef keychain) {
        Objects.requireNonNull(keychain)
        def keychains = listKeychains(domain)
        if (!keychains.contains(keychain) && keychains.add(keychain)) {
            setKeychains(keychains, domain)
            return true
        }
        false
    }

    @Override
    boolean remove(Object o) {
        Objects.requireNonNull(o)

        if (!KeychainRef.isInstance(o)) {
            throw new ClassCastException("expect object of type ${KeychainRef.name}")
        }

        KeychainRef k = o as KeychainRef
        def keychains = listKeychains(domain)
        if (keychains.remove(k)) {
            setKeychains(keychains, domain)
            return true
        }
        false
    }

    @Override
    boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c as Object)
        def keychains = c.collect {
            if (!KeychainRef.isInstance(it)) {
                throw new ClassCastException("expect object of type ${KeychainRef.name}")
            }
            it as KeychainRef
        }
        listKeychains(domain).containsAll(keychains)
    }

    @Override
    boolean addAll(Collection<? extends KeychainRef> c) {
        Objects.requireNonNull(c)
//        if(c.any {it == null }){
//            throw new NullPointerException("found null object in collection")
//        }
        def keychains = listKeychains(domain)
        if (keychains.addAll(c)) {
            setKeychains(keychains, domain)
            return true
        }
        false
    }

    @Override
    boolean addAll(int index, Collection<? extends KeychainRef> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c as Object)
        def keychainsToRemove = c.collect {
            if (!KeychainRef.isInstance(it)) {
                throw new ClassCastException("expect object of type ${KeychainRef.name}")
            }
            it as KeychainRef
        }
        def keychains = listKeychains(domain)
        def result = keychains.removeAll(keychainsToRemove)
        setKeychains(keychains, domain)
        result
    }

    @Override
    boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    void clear() {
        reset()
    }

    void reset() {
        resetKeychains(domain)
    }

    @Override
    KeychainRef get(int index) {
        listKeychains(domain).get(index)
    }

    @Override
    KeychainRef set(int index, KeychainRef element) {
        throw new UnsupportedOperationException()
    }

    @Override
    void add(int index, KeychainRef element) {
        throw new UnsupportedOperationException()
    }

    @Override
    KeychainRef remove(int index) {
        throw new UnsupportedOperationException()
    }

    @Override
    int indexOf(Object o) {
        Objects.requireNonNull(o)

        if (!KeychainRef.isInstance(o)) {
            throw new ClassCastException("expect object of type ${KeychainRef.name}")
        }

        listKeychains(domain).indexOf(o as KeychainRef)
    }

    @Override
    int lastIndexOf(Object o) {
        Objects.requireNonNull(o)

        if (!KeychainRef.isInstance(o)) {
            throw new ClassCastException("expect object of type ${KeychainRef.name}")
        }

        listKeychains(domain).lastIndexOf(o as KeychainRef)
    }

    @Override
    ListIterator<KeychainRef> listIterator() {
        listIterator(0)
    }

    @Override
    ListIterator<KeychainRef> listIterator(int index) {
        new KeychainLookupListIterator(listKeychains(domain).listIterator(index))
    }

    @Override
    List<KeychainRef> subList(int fromIndex, int toIndex) {
        listKeychains(domain).subList(fromIndex, toIndex)
    }

    KeychainRef getLoginKeyChain() {
        getLoginKeyChain(domain)
    }

    KeychainRef getDefaultKeyChain() {
        getDefaultKeyChain(domain)
    }

    static KeychainRef getLoginKeyChain(Domain domain) {
        KeychainRef.defaultKeychain()
    }

    static KeychainRef getDefaultKeyChain(Domain domain) {
        KeychainRef.defaultKeychain(domain)
    }

    static String expandPath(String path) {
        if (path.startsWith("~" + File.separator)) {
            path = System.getProperty("user.home") + path.substring(1)
        }
        path
    }

    static File expandPath(File path) {
        new File(expandPath(path.path))
    }

    static File canonical(File keychain) {
        expandPath(keychain).canonicalFile
    }
}
