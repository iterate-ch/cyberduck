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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoAuthenticationException;
import ch.cyberduck.core.cryptomator.VaultException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;

public interface Vault {

    /**
     * Create and open new vault
     *
     * @return Open vault
     * @throws VaultException                Failure parsing master key
     * @throws LoginCanceledException        User dismissed passphrase prompt
     * @throws BackgroundException           Failure reading master key from server
     * @throws NotfoundException             No master key file in home
     * @throws CryptoAuthenticationException Failure opening master key file
     */
    Vault create(final Session<?> session) throws BackgroundException;

    /**
     * Open existing vault
     *
     * @return Open vault
     * @throws VaultException                Failure parsing master key
     * @throws LoginCanceledException        User dismissed passphrase prompt
     * @throws BackgroundException           Failure reading master key from server
     * @throws NotfoundException             No master key file in home
     * @throws CryptoAuthenticationException Failure opening master key file
     */
    Vault load(final Session<?> session) throws BackgroundException;

    /**
     * Close vault
     */
    void close();

    /**
     * @param file Decrypted human readable path
     * @return True if the file is part of the vault
     */
    boolean contains(Path file);

    /**
     * @param file Decrypted human readable path
     * @return Encrypted path
     */
    Path encrypt(final Session<?> session, Path file) throws BackgroundException;

    /**
     * @param file     Decrypted human readable path
     * @param metadata Provide path to metadata of file if set to true
     * @return Encrypted path
     */
    Path encrypt(final Session<?> session, Path file, boolean metadata) throws BackgroundException;

    /**
     * @param directory Encrypted parent path
     * @param file      Encrypted path
     * @return Decrypted human readable path
     */
    Path decrypt(final Session<?> session, Path directory, Path file) throws BackgroundException;

    @SuppressWarnings("unchecked")
    <T> T getFeature(Session<?> session, Class<T> type, T delegate);

    Vault DISABLED = new Vault() {
        @Override
        public Vault create(final Session<?> session) throws BackgroundException {
            return this;
        }

        @Override
        public Vault load(final Session<?> session) throws BackgroundException {
            return this;
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
        public Path encrypt(final Session<?> session, final Path file) throws BackgroundException {
            return file;
        }

        @Override
        public Path encrypt(final Session<?> session, final Path file, final boolean metadata) throws BackgroundException {
            return file;
        }

        @Override
        public Path decrypt(final Session<?> session, final Path directory, final Path file) throws BackgroundException {
            return file;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getFeature(final Session<?> session, final Class<T> type, final T delegate) {
            return delegate;
        }
    };
}
