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
            final TransferStatus headerStatus = new TransferStatus(status);
            headerStatus.setOffset(0);
            final InputStream in = proxy.read(encrypted, headerStatus.length(status.isAppend() ?
                    cryptor.fileHeaderCryptor().headerSize() :
                    vault.toCiphertextSize(status.getLength())), callback);
            final ByteBuffer headerBuffer = ByteBuffer.allocate(cryptor.fileHeaderCryptor().headerSize());
            final int read = IOUtils.read(in, headerBuffer.array());
            final FileHeader header = cryptor.fileHeaderCryptor().decryptHeader(headerBuffer);
            if(status.isAppend()) {
                IOUtils.closeQuietly(in);
                final TransferStatus s = new TransferStatus(status).length(-1L);
                s.setOffset(this.align(status.getOffset()));
                final CryptoInputStream crypto = new CryptoInputStream(proxy.read(encrypted, s, callback), cryptor, header, vault.numberOfChunks(status.getOffset()) - 1);
                crypto.skip(this.position(status.getOffset()) - s.getOffset());
                return crypto;
            }
            else {
                return new CryptoInputStream(in, cryptor, header, vault.numberOfChunks(status.getOffset()));
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private long align(final long offset) {
        final long chunk = offset / vault.getCryptor().fileContentCryptor().cleartextChunkSize();
        return vault.getCryptor().fileHeaderCryptor().headerSize() + chunk * vault.getCryptor().fileContentCryptor().ciphertextChunkSize();
    }

    private long position(final long offset) {
        return vault.toCiphertextSize(offset) -
                (vault.getCryptor().fileContentCryptor().ciphertextChunkSize() - vault.getCryptor().fileContentCryptor().cleartextChunkSize());
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return proxy.offset(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoReadFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
