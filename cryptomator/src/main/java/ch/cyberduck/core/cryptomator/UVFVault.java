package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryV7Provider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.UVFMasterkey;

import java.util.EnumSet;

public class UVFVault implements Vault {

    private static final Logger log = LogManager.getLogger(UVFVault.class);

    /**
     * Root of vault directory
     */
    private final Path home;
    private final Path vault;

    private final String decrypted;
    private Cryptor cryptor;
    private CryptorCache fileNameCryptor;
    private CryptoFilename filenameProvider;
    private CryptoDirectory directoryProvider;

    public UVFVault(final Path home, final String decryptedPayload) {
        this.home = home;
        this.decrypted = decryptedPayload;
        // New vault home with vault flag set for internal use
        final EnumSet<Path.Type> type = EnumSet.copyOf(home.getType());
        type.add(Path.Type.vault);
        if(home.isRoot()) {
            this.vault = new Path(home.getAbsolute(), type, new PathAttributes(home.attributes()));
        }
        else {
            this.vault = new Path(home.getParent(), home.getName(), type, new PathAttributes(home.attributes()));
        }
    }

    @Override
    public Path create(final Session<?> session, final String region, final VaultCredentials credentials) throws BackgroundException {
        return null;
    }

    // load -> unlock -> open
    @Override
    public UVFVault load(final Session<?> session, final PasswordCallback prompt) throws BackgroundException {
        UVFMasterkey masterKey = UVFMasterkey.fromDecryptedPayload(this.decrypted);

        final CryptorProvider provider = CryptorProvider.forScheme(CryptorProvider.Scheme.UVF_DRAFT);
        log.debug("Initialized crypto provider {}", provider);
        this.cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
        this.fileNameCryptor = new CryptorCache(cryptor.fileNameCryptor());
        this.filenameProvider = new CryptoFilenameV7Provider(/* TODO threshold was previously defined in vault.config - default now? */);
        this.directoryProvider = new CryptoDirectoryV7Provider(vault, filenameProvider, fileNameCryptor);
        //TODO where? this.nonceSize = vaultConfig.getNonceSize();
        return this;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean contains(final Path file) {
        return false;
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file) throws BackgroundException {
        return null;
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file, final boolean metadata) throws BackgroundException {
        return null;
    }

    @Override
    public Path decrypt(final Session<?> session, final Path file) throws BackgroundException {
        return null;
    }

    @Override
    public long toCiphertextSize(final long cleartextFileOffset, final long cleartextFileSize) {
        return 0;
    }

    @Override
    public long toCleartextSize(final long cleartextFileOffset, final long ciphertextFileSize) throws BackgroundException {
        return 0;
    }

    @Override
    public <T> T getFeature(final Session<?> session, final Class<T> type, final T delegate) {
        return null;
    }

    @Override
    public State getState() {
        return null;
    }

    @Override
    public Path getHome() {
        return null;
    }
}
