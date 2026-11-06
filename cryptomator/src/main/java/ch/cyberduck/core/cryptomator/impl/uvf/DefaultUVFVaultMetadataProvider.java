package ch.cyberduck.core.cryptomator.impl.uvf;

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

import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.vault.VaultCredentials;

import org.cryptomator.cryptolib.api.UvfKeyEncryptionKey;
import org.cryptomator.cryptolib.api.UvfLockedVault;
import org.cryptomator.cryptolib.api.UvfRecoveryKey;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;

/**
 * Password protected vault. The key encryption key is derived from the password using Argon2id. A recovery key is
 * generated for new vaults compatible with Cryptomator.
 */
public class DefaultUVFVaultMetadataProvider implements UVFVaultMetadataProvider {

    private final VaultCredentials passphrase;

    public DefaultUVFVaultMetadataProvider(final VaultCredentials passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public UvfUnlockedVault unlock(final UvfLockedVault metadata) throws BackgroundException {
        return metadata.unlock(passphrase.getPassword(), FastSecureRandomProvider.get().provide());
    }

    @Override
    public UvfUnlockedVault create() throws BackgroundException {
        // TODO Display recovery key from serializedRecoveryKey() to the user
        return UvfUnlockedVault.create(FastSecureRandomProvider.get().provide(),
                UvfKeyEncryptionKey.password(passphrase.getPassword()),
                UvfRecoveryKey.generate(FastSecureRandomProvider.get().provide()));
    }

    @Override
    public void close() {
        passphrase.reset();
    }
}
