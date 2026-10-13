package ch.cyberduck.core.azure;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Optional;

/**
 * Directories are no blobs in hierarchical namespace and created with Data Lake Storage API. Files are copied
 * with Blob Storage API.
 */
public class AzureDataLakeCopyFeature implements Copy {

    private final AzureSession session;
    private final AzureBlobCopyFeature blob;

    public AzureDataLakeCopyFeature(final AzureSession session) {
        this.session = session;
        this.blob = new AzureBlobCopyFeature(session);
    }

    @Override
    public Path copy(final Path source, final Path copy, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        if(source.isDirectory()) {
            // Children are copied individually.
            if(!status.isExists()) {
                return new AzureDataLakeDirectoryFeature(session).mkdir(new AzureWriteFeature(session), copy, status);
            }
            return copy;
        }
        return blob.copy(source, copy, status, callback, listener);
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        blob.preflight(source, target);
    }
}
