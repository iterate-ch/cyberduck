package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
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

import com.microsoft.azure.storage.OperationContext;

public class AzureMoveFeature implements Move {

    private final AzureSession session;
    private final OperationContext context;

    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    public AzureMoveFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return new AzureCopyFeature(session, context).isSupported(source, target)
                && new AzureDeleteFeature(session, context).isSupported(source);
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        final Path copy = new AzureCopyFeature(session, context).copy(file, renamed, new TransferStatus().withLength(file.attributes().getSize()), connectionCallback, new DisabledStreamListener());
        new AzureDeleteFeature(session, context).delete(Collections.singletonList(file), connectionCallback, callback);
        return copy;
    }
}
