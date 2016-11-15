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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public class AzureDirectoryFeature implements Directory {

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
            = new AzurePathContainerService();

    public AzureDirectoryFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public void mkdir(Path file) throws BackgroundException {
        this.mkdir(file, null, null);
    }

    @Override
    public void mkdir(final Path file, final String region, final TransferStatus status) throws BackgroundException {
        try {
            final BlobRequestOptions options = new BlobRequestOptions();
            if(containerService.isContainer(file)) {
                // Container name must be lower case.
                final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                container.create(options, context);
            }
            else {
                // Create delimiter placeholder
                final CloudBlockBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlockBlobReference(containerService.getKey(file).concat(String.valueOf(Path.DELIMITER)));
                blob.upload(new ByteArrayInputStream(new byte[]{}), 0L, AccessCondition.generateEmptyCondition(), options, context);
            }
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}
