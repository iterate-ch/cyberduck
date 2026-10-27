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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.CiphertextLocal;
import ch.cyberduck.core.cryptomator.CryptoStreamListener;
import ch.cyberduck.core.cryptomator.CryptoTransferStatus;
import ch.cyberduck.core.cryptomator.random.SeededNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.FileHeader;

import java.util.EnumSet;

public class CryptoUploadFeature<Reply> implements Upload<Reply> {

    private final Session<?> session;
    private final Upload<Reply> proxy;
    private final AbstractVault vault;

    public CryptoUploadFeature(final Session<?> session, final Upload<Reply> delegate, final AbstractVault vault) {
        this.session = session;
        this.proxy = delegate;
        this.vault = vault;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Reply upload(final Write<Reply> write, final Path file, final Local local, final BandwidthThrottle throttle, final ProgressListener progress, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(null == status.getHeader()) {
            final FileHeader header = vault.getFileHeaderCryptor().create();
            status.setHeader(vault.getFileHeaderCryptor().encryptHeader(header));
        }
        if(null == status.getNonces()) {
            status.setNonces(new SeededNonceGenerator(vault.getNonceSize()));
        }
        // Upload the encrypted content read from the local file with the status for the encrypted content
        final TransferStatus encrypted = new CryptoTransferStatus(vault, file, status);
        return proxy.upload((Write<Reply>) session._getFeature(Write.class), vault.encrypt(session, file),
                new CiphertextLocal(local, vault.getFileContentCryptor(), vault.getFileHeaderCryptor(), status.getHeader(), status.getNonces()),
                throttle, progress, new CryptoStreamListener(listener, status.getLength(), encrypted.getLength()), encrypted, callback);
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return proxy.append(vault.encrypt(session, file), status.setParent(new CryptoTransferStatus(vault, file, status)));
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        if(file.isDirectory()) {
            return proxy.checksum(file, status);
        }
        return new CryptoChecksumCompute(proxy.checksum(file, status), vault);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) {
        return proxy.features(file, status);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoUploadFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
