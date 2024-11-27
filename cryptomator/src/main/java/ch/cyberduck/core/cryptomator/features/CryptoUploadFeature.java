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
import ch.cyberduck.core.cryptomator.CryptoTransferStatus;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

public class CryptoUploadFeature<Reply> implements Upload<Reply> {

    private final Session<?> session;
    private final Upload<Reply> proxy;
    private final CryptoVault vault;

    public CryptoUploadFeature(final Session<?> session, final Upload<Reply> delegate, final Write<Reply> writer, final CryptoVault vault) {
        this.session = session;
        this.proxy = delegate.withWriter(new CryptoWriteFeature<>(session, writer, vault));
        this.vault = vault;
    }

    @Override
    public Reply upload(final Path file, final Local local, final BandwidthThrottle throttle, final ProgressListener progress, final StreamListener streamListener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return proxy.upload(vault.encrypt(session, file), local, throttle, progress, streamListener, status.withDestinationLength(new CryptoTransferStatus(vault, status).getLength()), callback);
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return proxy.append(vault.encrypt(session, file), status.withDestinationLength(new CryptoTransferStatus(vault, status).getLength()));
    }

    @Override
    public Upload<Reply> withWriter(final Write<Reply> writer) {
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoUploadFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
