package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.random.NonceGenerator;

import org.cryptomator.cryptolib.api.FileContentCryptor;
import org.cryptomator.cryptolib.api.FileHeaderCryptor;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Local file that is read as the encrypted content of the file in a vault.
 */
public class CiphertextLocal extends Local {

    private final Local cleartext;
    private final FileContentCryptor cryptor;
    private final FileHeaderCryptor headerCryptor;
    private final ByteBuffer encrypted;
    private final NonceGenerator nonces;

    /**
     * @param cleartext Local file with the cleartext content
     * @param cryptor   Content cryptor
     * @param headerCryptor File header cryptor
     * @param encrypted Encrypted file header written at the start of the ciphertext
     * @param nonces    Nonce for a chunk by index
     */
    public CiphertextLocal(final Local cleartext, final FileContentCryptor cryptor, final FileHeaderCryptor headerCryptor,
                           final ByteBuffer encrypted, final NonceGenerator nonces) {
        super(cleartext.getAbsolute());
        this.cleartext = cleartext;
        this.cryptor = cryptor;
        this.headerCryptor = headerCryptor;
        this.encrypted = encrypted;
        this.nonces = nonces;
    }

    @Override
    public InputStream getInputStream() throws AccessDeniedException {
        // Decrypt the header for every stream. A cipher refuses to encrypt with the same key and nonce again directly
        // after it was used with the same parameters, which includes reading the same chunk again.
        return new CiphertextInputStream(cleartext.getInputStream(), cryptor, headerCryptor.decryptHeader(encrypted), encrypted, nonces);
    }
}
