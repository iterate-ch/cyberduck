package ch.cyberduck.core.cryptomator.impl.uvf;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.vault.VaultException;
import ch.cyberduck.core.vault.VaultMetadataProvider;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.cryptomator.cryptolib.api.DirectoryContentCryptor;
import org.cryptomator.cryptolib.api.DirectoryMetadata;
import org.cryptomator.cryptolib.api.UVFMasterkey;

public interface UVFVaultMetadataProvider extends VaultMetadataProvider {

    String computeRootDirIdHash();

    byte[] computeRootDirUvf();

    /**
     * @return The encrypted byte array representing the secured payload.
     * @throws VaultException If the encryption process encounters an invalid state or an unexpected error occurs.
     */
    String encrypt() throws VaultException;

    /**
     * @return The decrypted payload.
     * @throws VaultException If an error occurs during the decryption process, such as an invalid state or corrupted data.
     */
    String decrypt() throws VaultException;

    default String computeRootDirIdHash(final String payloadJSON) {
        final UVFMasterkey masterKey = UVFMasterkey.fromDecryptedPayload(payloadJSON);
        final CryptorProvider provider = CryptorProvider.forScheme(CryptorProvider.Scheme.UVF_DRAFT);
        final Cryptor cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
        final byte[] rootDirId = masterKey.rootDirId();
        return cryptor.fileNameCryptor(masterKey.firstRevision()).hashDirectoryId(rootDirId);
    }

    default byte[] computeRootDirUvf(final String payloadJSON) {
        final UVFMasterkey masterKey = UVFMasterkey.fromDecryptedPayload(payloadJSON);
        final CryptorProvider provider = CryptorProvider.forScheme(CryptorProvider.Scheme.UVF_DRAFT);
        final Cryptor cryptor = provider.provide(masterKey, FastSecureRandomProvider.get().provide());
        DirectoryMetadata rootDirMetadata = cryptor.directoryContentCryptor().rootDirectoryMetadata();
        DirectoryContentCryptor dirContentCryptor = cryptor.directoryContentCryptor();
        return dirContentCryptor.encryptDirectoryMetadata(rootDirMetadata);
    }

    static UVFVaultMetadataProvider cast(VaultMetadataProvider provider) throws ConnectionCanceledException {
        if(provider instanceof UVFVaultMetadataProvider) {
            return (UVFVaultMetadataProvider) provider;
        }
        else {
            throw new ConnectionCanceledException(new UnsupportedException(provider.getClass().getName()));
        }
    }
}
