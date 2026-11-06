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
import ch.cyberduck.core.vault.VaultException;

import org.cryptomator.cryptolib.api.UvfKeyEncryptionKey;
import org.cryptomator.cryptolib.api.UvfLockedVault;
import org.cryptomator.cryptolib.api.UvfRecoveryKey;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;

import javax.crypto.SecretKey;
import java.util.Optional;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;

/**
 * Vault with member key provided by key management service such as Cryptomator Hub. The key set contains the
 * symmetric member key and the recovery key. The private part of the recovery key is only required to create a vault.
 */
public class JWKSetUVFVaultMetadataProvider implements UVFVaultMetadataProvider {

    private final JWKSet keys;

    public JWKSetUVFVaultMetadataProvider(final JWKSet keys) {
        this.keys = keys;
    }

    @Override
    public UvfUnlockedVault unlock(final UvfLockedVault metadata) throws BackgroundException {
        return metadata.unlock(this.getMemberKey(), FastSecureRandomProvider.get().provide());
    }

    @Override
    public UvfUnlockedVault create() throws BackgroundException {
        final ECKey key = this.find(ECKey.class).orElseThrow(() -> new VaultException("Missing recovery key"));
        try {
            final UvfRecoveryKey recoveryKey = key.isPrivate() ?
                    UvfRecoveryKey.of(key.toKeyPair()) : UvfRecoveryKey.of(key.toECPublicKey());
            return UvfUnlockedVault.create(FastSecureRandomProvider.get().provide(),
                    UvfKeyEncryptionKey.memberKey(this.getMemberKey()), recoveryKey);
        }
        catch(JOSEException e) {
            throw new VaultException("Invalid recovery key", e);
        }
    }

    private SecretKey getMemberKey() throws VaultException {
        return this.find(OctetSequenceKey.class).orElseThrow(() -> new VaultException("Missing member key")).toSecretKey("AES");
    }

    private <K extends JWK> Optional<K> find(final Class<K> type) {
        return keys.getKeys().stream().filter(type::isInstance).map(type::cast).findFirst();
    }

    @Override
    public void close() {
        //
    }
}
