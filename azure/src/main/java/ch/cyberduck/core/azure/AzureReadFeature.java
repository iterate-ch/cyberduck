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
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

import com.azure.core.exception.HttpResponseException;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;

public class AzureReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(AzureReadFeature.class);

    private final AzureSession session;
    private final PathContainerService containerService
        = new DirectoryDelimiterPathContainerService();

    public AzureReadFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public boolean offset(final Path file) {
        return true;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(status.isAppend()) {
                return session.getClient().getBlobContainerClient(containerService.getContainer(file).getName())
                        .getBlobClient(containerService.getKey(file)).openInputStream(new BlobRange(status.getOffset()), new BlobRequestConditions());
            }
            else {
                return session.getClient().getBlobContainerClient(containerService.getContainer(file).getName())
                        .getBlobClient(containerService.getKey(file)).openInputStream();
            }
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Download {0} failed", e, file);
        }
    }
}
