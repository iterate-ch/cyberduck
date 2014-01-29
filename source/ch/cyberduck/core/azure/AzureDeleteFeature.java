package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;

import java.net.URISyntaxException;
import java.util.List;

import com.microsoft.windowsazure.services.blob.client.BlobRequestOptions;
import com.microsoft.windowsazure.services.blob.client.DeleteSnapshotsOption;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id$
 */
public class AzureDeleteFeature implements Delete {

    private AzureSession session;

    private PathContainerService containerService
            = new PathContainerService();

    public AzureDeleteFeature(AzureSession session) {
        this.session = session;
    }

    @Override
    public void delete(List<Path> files, LoginCallback prompt) throws BackgroundException {
        for(Path file : files) {
            try {
                final BlobRequestOptions options = new BlobRequestOptions();
                options.setRetryPolicyFactory(new RetryNoRetry());
                if(containerService.isContainer(file)) {
                    session.getClient().getContainerReference(containerService.getContainer(file).getName()).delete(options, null);
                }
                else {
                    if(file.attributes().isPlaceholder()) {
                        session.getClient().getContainerReference(containerService.getContainer(file).getName())
                                .getBlockBlobReference(containerService.getKey(file).concat(String.valueOf(Path.DELIMITER))).delete(
                                DeleteSnapshotsOption.NONE, null, options, null);
                    }
                    else {
                        session.getClient().getContainerReference(containerService.getContainer(file).getName())
                                .getBlockBlobReference(containerService.getKey(file)).delete(
                                DeleteSnapshotsOption.NONE, null, options, null);
                    }
                }
            }
            catch(StorageException e) {
                throw new AzureExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(URISyntaxException e) {
                throw new NotfoundException(e.getMessage(), e);
            }
        }
    }
}
