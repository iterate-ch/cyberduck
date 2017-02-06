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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

public class CryptoDownloadFeature implements Download {

    private final Session<?> session;
    private final Download proxy;
    private final Vault vault;

    public CryptoDownloadFeature(final Session<?> session, final Download proxy, final Read reader, final CryptoVault vault) {
        this.session = session;
        this.proxy = proxy.withReader(new CryptoReadFeature(session, reader, vault));
        this.vault = vault;
    }

    @Override
    public void download(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        proxy.download(vault.encrypt(session, file), local, throttle, listener, status, callback);
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        try {
            return proxy.offset(vault.encrypt(session, file));
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public Download withReader(final Read reader) {
        return this;
    }
}