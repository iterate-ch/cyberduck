package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

public interface Vault {

    /**
     * Create and open new vault
     *
     * @param home     Target for vault
     * @param keychain Password store
     * @param callback Password prompt
     */
    void create(Path home, PasswordStore keychain, LoginCallback callback) throws BackgroundException;

    /**
     * Open existing vault
     *
     * @param home     Target for vault
     * @param keychain Password store
     * @param callback Password prompt
     */
    void load(Path home, PasswordStore keychain, LoginCallback callback) throws BackgroundException;

    /**
     * Close vault
     */
    void close();

    /**
     * @param file File or directory
     * @return True if the file is part of the vault
     */
    boolean contains(Path file);

    Path encrypt(Path file) throws BackgroundException;

    Path decrypt(Path directory, Path file) throws BackgroundException;

    @SuppressWarnings("unchecked")
    <T> T getFeature(Class<T> type, T delegate);

    Vault DISABLED = new Vault() {
        @Override
        public void create(final Path home, final PasswordStore keychain, final LoginCallback callback) throws BackgroundException {
            throw new AccessDeniedException();
        }

        @Override
        public void load(final Path home, final PasswordStore keychain, final LoginCallback callback) throws BackgroundException {
            throw new AccessDeniedException();
        }

        @Override
        public void close() {
            //
        }

        @Override
        public boolean contains(final Path file) {
            return false;
        }

        @Override
        public Path encrypt(final Path file) throws BackgroundException {
            return file;
        }

        @Override
        public Path decrypt(final Path directory, final Path file) throws BackgroundException {
            return file;
        }

        @Override
        public <T> T getFeature(final Class<T> type, final T delegate) {
            return delegate;
        }
    };
}
