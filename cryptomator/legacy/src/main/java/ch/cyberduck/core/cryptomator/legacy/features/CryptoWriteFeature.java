package ch.cyberduck.core.cryptomator.legacy.features;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoOutputStream;
import ch.cyberduck.core.cryptomator.CryptoTransferStatus;
import ch.cyberduck.core.cryptomator.legacy.CryptomatorVault;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.cryptomator.random.RotatingNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.random.NonceGenerator;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.util.EnumSet;

public class CryptoWriteFeature<Reply> implements Write<Reply> {
    private static final Logger log = LogManager.getLogger(CryptoWriteFeature.class);

    private final Session<?> session;
    private final Write<Reply> proxy;
    private final CryptomatorVault cryptomator;

    public CryptoWriteFeature(final Session<?> session, final Write<Reply> proxy, final CryptomatorVault cryptomator) {
        this.session = session;
        this.proxy = proxy;
        this.cryptomator = cryptomator;
    }

    @Override
    public StatusOutputStream<Reply> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(file.isDirectory()) {
                // When creating directory placeholder files, no content must be added
                return proxy.write(cryptomator.encrypt(session, file), status, callback);
            }
            if(null == status.getHeader()) {
                final FileHeader header = cryptomator.getFileHeaderCryptor().create();
                status.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
            }
            if(null == status.getNonces()) {
                final NonceGenerator nonces = status.getLength() == TransferStatus.UNKNOWN_LENGTH ?
                        new RandomNonceGenerator(cryptomator.getNonceSize()) :
                        new RotatingNonceGenerator(cryptomator.getNonceSize(), cryptomator.numberOfChunks(status.getLength()));
                log.debug("Using {}", nonces);
                status.setNonces(nonces);
            }
            final StatusOutputStream<Reply> stream = proxy.write(cryptomator.encrypt(session, file),
                    new CryptoTransferStatus(cryptomator, file, status), callback);
            if(status.getOffset() == 0L) {
                stream.write(status.getHeader().array());
            }
            return new StatusOutputStream<Reply>(new CryptoOutputStream(stream,
                    cryptomator.getFileContentCryptor(), cryptomator.getFileHeaderCryptor().decryptHeader(status.getHeader()),
                    status.getNonces(), cryptomator.numberOfChunks(status.getOffset()))) {
                @Override
                public Reply getStatus() throws BackgroundException {
                    return stream.getStatus();
                }
            };
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return proxy.features(file);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        if(file.isDirectory()) {
            return proxy.checksum(file, status);
        }
        return new CryptoChecksumCompute(proxy.checksum(file, status), cryptomator);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        proxy.preflight(cryptomator.encrypt(session, file));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoWriteFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
