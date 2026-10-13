package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Optional;

public class AzureCopyFeature implements Copy {

    private final AzureSession session;

    public AzureCopyFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path copy, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        return this.proxy().copy(source, copy, status, callback, listener);
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        this.proxy().preflight(source, target);
    }

    private Copy proxy() {
        if(session.getStorageAccountInfo().isHierarchicalNamespaceEnabled()) {
            return new AzureDataLakeCopyFeature(session);
        }
        return new AzureBlobCopyFeature(session);
    }
}
