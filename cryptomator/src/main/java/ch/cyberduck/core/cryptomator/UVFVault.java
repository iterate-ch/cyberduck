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
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryV7Provider;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;
import org.cryptomator.cryptolib.api.UVFMasterkey;

import java.util.EnumSet;
import java.util.Objects;

public class UVFVault extends AbstractVault {

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

    private int nonceSize;

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
        throw new UnsupportedOperationException();
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
        this.nonceSize = 12;
        return this;
    }

    @Override
    public synchronized void close() {
        super.close();
        cryptor = null;
        fileNameCryptor = null;
    }

    @Override
    public Path getMasterkey() {
        //TODO: implement
        return null;
    }

    @Override
    public Path getConfig() {
        //TODO: implement
        return null;
    }

    @Override
    public Path gethHome() {
        return home;
    }

    @Override
    public FileHeaderCryptor getFileHeaderCryptor() {
        return cryptor.fileHeaderCryptor();
    }

    @Override
    public FileContentCryptor getFileContentCryptor() {
        return cryptor.fileContentCryptor();
    }

    @Override
    public CryptorCache getFileNameCryptor() {
        return fileNameCryptor;
    }

    @Override
    public CryptoFilename getFilenameProvider() {
        return filenameProvider;
    }

    @Override
    public CryptoDirectory getDirectoryProvider() {
        return directoryProvider;
    }

    @Override
    public Cryptor getCryptor() {
        return cryptor;
    }

    @Override
    public int getNonceSize() {
        return nonceSize;
    }

    @Override
    public int getVersion() {
        return VAULT_VERSION;
    }

    @Override
    public Path getHome() {
        return home;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof UVFVault)) {
            return false;
        }
        final UVFVault that = (UVFVault) o;
        return new SimplePathPredicate(home).test(that.home);
    }

    @Override
    public int hashCode() {
        return Objects.hash(new SimplePathPredicate(home));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UVFVault{");
        sb.append("home=").append(home);
        sb.append(", cryptor=").append(cryptor);
        sb.append('}');
        return sb.toString();
    }
}
