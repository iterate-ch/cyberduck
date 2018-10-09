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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import java.net.URISyntaxException;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureFindFeature implements Find {

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
        = new AzurePathContainerService();

    public AzureFindFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public boolean find(Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            try {
                final boolean found;
                if(containerService.isContainer(file)) {
                    final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                    found = container.exists(null, null, context);
                }
                else {
                    final CloudBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlobReferenceFromServer(containerService.getKey(file));
                    found = blob.exists(null, null, context);
                }
                return found;
            }
            catch(StorageException e) {
                throw new AzureExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
            catch(URISyntaxException e) {
                return false;
            }
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        return this;
    }
}
