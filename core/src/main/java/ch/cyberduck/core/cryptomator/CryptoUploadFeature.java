package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

public class CryptoUploadFeature<Output> implements Upload<Output> {

    private final Session<?> session;
    private final Upload<Output> delegate;
    private final CryptoVault vault;

    public CryptoUploadFeature(final Session<?> session, final Upload<Output> delegate, final CryptoVault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public Output upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(vault.contains(file)) {
            // Write header
            final Cryptor cryptor = vault.getCryptor();
            final FileHeader header = cryptor.fileHeaderCryptor().create();
            status.setHeader(header);
        }
        return delegate.upload(vault.encrypt(session, file), local, throttle, listener, status, callback);
    }

    @Override
    public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        return delegate.append(vault.encrypt(session, file), length, cache);
    }
}
