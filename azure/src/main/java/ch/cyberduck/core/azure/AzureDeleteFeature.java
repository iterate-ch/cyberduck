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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.DeleteSnapshotsOption;

public class AzureDeleteFeature implements Delete {

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
            = new AzurePathContainerService();

    public AzureDeleteFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public void delete(final List<Path> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> containers = new ArrayList<Path>();
        for(Path file : files) {
            if(containerService.isContainer(file)) {
                containers.add(file);
            }
            else {
                callback.delete(file);
                try {
                    final BlobRequestOptions options = new BlobRequestOptions();
                    session.getClient().getContainerReference(containerService.getContainer(file).getName())
                            .getBlockBlobReference(containerService.getKey(file)).delete(
                            DeleteSnapshotsOption.INCLUDE_SNAPSHOTS, AccessCondition.generateEmptyCondition(), options, context);
                }
                catch(StorageException e) {
                    throw new AzureExceptionMappingService().map("Cannot delete {0}", e, file);
                }
                catch(URISyntaxException e) {
                    throw new NotfoundException(e.getMessage(), e);
                }
            }
        }
        for(Path file : containers) {
            callback.delete(file);
            try {
                final BlobRequestOptions options = new BlobRequestOptions();
                session.getClient().getContainerReference(containerService.getContainer(file).getName()).delete(
                        AccessCondition.generateEmptyCondition(), options, context);
            }
            catch(StorageException e) {
                throw new AzureExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(URISyntaxException e) {
                throw new NotfoundException(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }

    @Override
    public boolean isRecursive() {
        return false;
    }
}
