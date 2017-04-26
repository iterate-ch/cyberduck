package ch.cyberduck.core.vault.registry;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultRegistry;

public class VaultRegistryUploadFeature<Output> implements Upload<Output> {

    private final Session<?> session;
    private final Upload<Output> proxy;
    private final VaultRegistry registry;

    public VaultRegistryUploadFeature(final Session<?> session, final Upload<Output> proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Output upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return (Output) registry.find(session, file).getFeature(session, Upload.class, proxy).upload(file, local, throttle, listener, status, callback);
    }

    @Override
    public Write.Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        return registry.find(session, file).getFeature(session, Upload.class, proxy).append(file, length, cache);
    }

    @Override
    public Upload<Output> withWriter(final Write<Output> writer) {
        proxy.withWriter(writer);
        return this;
    }
}
