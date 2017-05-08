package ch.cyberduck.core.cryptomator.features;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoInputStream;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class CryptoReadFeature implements Read {

    private final Session<?> session;
    private final Read proxy;
    private final CryptoVault vault;

    public CryptoReadFeature(final Session<?> session, final Read proxy, final CryptoVault vault) {
        this.session = session;
        this.proxy = proxy;
        this.vault = vault;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final Path encrypted = vault.encrypt(session, file);
            // Header
            final Cryptor cryptor = vault.getCryptor();
            final InputStream proxy = this.proxy.read(encrypted,
                    new TransferStatus(status).length(vault.toCiphertextSize(status.getLength())), callback);
            final ByteBuffer headerBuffer = ByteBuffer.allocate(cryptor.fileHeaderCryptor().headerSize());
            final int read = IOUtils.read(proxy, headerBuffer.array());
            final FileHeader header = cryptor.fileHeaderCryptor().decryptHeader(headerBuffer);
            // Content
            return new CryptoInputStream(proxy, cryptor, header, vault.numberOfChunks(status.getOffset()));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoReadFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
