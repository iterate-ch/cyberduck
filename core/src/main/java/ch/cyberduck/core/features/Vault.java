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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.vault.DisabledVault;
import ch.cyberduck.core.vault.VaultCredentials;

public interface Vault {

    /**
     * Create and open new vault
     *
     * @return Open vault
     * @throws LoginCanceledException User dismissed passphrase prompt
     * @throws BackgroundException    Failure reading master key from server
     * @throws NotfoundException      No master key file in home
     */
    Vault create(Session<?> session, String region, final VaultCredentials credentials) throws BackgroundException;

    /**
     * Open existing vault
     *
     * @return Open vault
     * @throws LoginCanceledException User dismissed passphrase prompt
     * @throws BackgroundException    Failure reading master key from server
     * @throws NotfoundException      No master key file in home
     */
    Vault load(Session<?> session, final PasswordCallback prompt) throws BackgroundException;

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
    Path encrypt(Session<?> session, Path file) throws BackgroundException;

    /**
     * @param file     Decrypted human readable path
     * @param metadata Provide path to metadata of file if set to true
     * @return Encrypted path
     */
    Path encrypt(Session<?> session, Path file, boolean metadata) throws BackgroundException;

    /**
     * @param file      Encrypted path
     * @return Decrypted human readable path
     */
    Path decrypt(Session<?> session, Path file) throws BackgroundException;

    long toCiphertextSize(long cleartextFileSize);

    long toCleartextSize(long ciphertextFileSize) throws BackgroundException;

    @SuppressWarnings("unchecked")
    <T> T getFeature(Session<?> session, Class<T> type, T delegate);

    Vault DISABLED = new DisabledVault();

    State getState();

    Path getHome();

    enum State {
        open,
        closed
    }
}
