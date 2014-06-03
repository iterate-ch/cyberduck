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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.blob.client.BlobRequestOptions;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id$
 */
public class AzureDirectoryFeature implements Directory {

    private AzureSession session;

    private PathContainerService containerService
            = new AzurePathContainerService();

    public AzureDirectoryFeature(AzureSession session) {
        this.session = session;
    }

    @Override
    public void mkdir(Path file) throws BackgroundException {
        this.mkdir(file, null);
    }

    @Override
    public void mkdir(final Path file, final String region) throws BackgroundException {
        try {
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setRetryPolicyFactory(new RetryNoRetry());
            if(containerService.isContainer(file)) {
                // Container name must be lower case.
                final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                container.create(options, null);
            }
            else {
                // Create delimiter placeholder
                final CloudBlockBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlockBlobReference(containerService.getKey(file));
                blob.upload(new ByteArrayInputStream(new byte[]{}), 0L, null, options, null);
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
