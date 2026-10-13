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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;
import java.util.Optional;

public class AzureMoveFeature implements Move {

    private final AzureSession session;

    public AzureMoveFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        this.proxy().preflight(source, target);
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        return this.proxy().move(file, renamed, status, callback, connectionCallback);
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return this.proxy().features(source, target);
    }

    private Move proxy() {
        if(session.getStorageAccountInfo().isHierarchicalNamespaceEnabled()) {
            return new AzureDataLakeMoveFeature(session);
        }
        return new AzureBlobMoveFeature(session);
    }
}
