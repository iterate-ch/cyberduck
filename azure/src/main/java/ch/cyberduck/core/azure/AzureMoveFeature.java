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
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Collections;

public class AzureMoveFeature implements Move {

    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    private final AzureCopyFeature proxy;
    private final AzureDeleteFeature delete;

    public AzureMoveFeature(final AzureSession session) {
        this.proxy = new AzureCopyFeature(session);
        this.delete = new AzureDeleteFeature(session);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        proxy.preflight(source, target);
        delete.preflight(source);
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        final Path copy = proxy.copy(file, renamed, new TransferStatus().withLength(file.attributes().getSize()), connectionCallback, new DisabledStreamListener());
        delete.delete(Collections.singletonList(file), connectionCallback, callback);
        return copy;
    }
}
