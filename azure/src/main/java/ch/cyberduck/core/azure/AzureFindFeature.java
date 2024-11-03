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

import ch.cyberduck.core.CancellingListProgressListener;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

public class AzureFindFeature implements Find {
    private static final Logger log = LogManager.getLogger(AzureFindFeature.class);

    private final AzureSession session;
    private final OperationContext context;

    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

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
                    return container.exists(null, null, context);
                }
                if(file.isFile() || file.isPlaceholder()) {
                    try {
                        final CloudBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                                .getBlobReferenceFromServer(containerService.getKey(file));
                        return blob.exists(null, null, context);
                    }
                    catch(StorageException e) {
                        switch(e.getHttpStatusCode()) {
                            case HttpStatus.SC_NOT_FOUND:
                                if(file.isPlaceholder()) {
                                    // Ignore failure and look for common prefix
                                    break;
                                }
                            default:
                                throw e;
                        }
                    }
                }
                log.debug("Search for common prefix {}", file);
                // Check for common prefix
                try {
                    new AzureObjectListService(session, context).list(file, new CancellingListProgressListener());
                    return true;
                }
                catch(ListCanceledException l) {
                    // Found common prefix
                    return true;
                }
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
}
