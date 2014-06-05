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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Headers;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.windowsazure.services.blob.client.BlobRequestOptions;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id$
 */
public class AzureMetadataFeature implements Headers {

    private AzureSession session;

    private PathContainerService containerService
            = new AzurePathContainerService();

    public AzureMetadataFeature(AzureSession session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getMetadata(Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                container.downloadAttributes();
                return container.getMetadata();
            }
            else {
                final CloudBlockBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlockBlobReference(containerService.getKey(file));
                blob.downloadAttributes();
                return blob.getMetadata();
            }
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot read file attributes", e, file);
        }
    }

    @Override
    public void setMetadata(Path file, Map<String, String> metadata) throws BackgroundException {
        try {
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setRetryPolicyFactory(new RetryNoRetry());
            if(containerService.isContainer(file)) {
                final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                container.setMetadata(new HashMap<String, String>(metadata));
                container.uploadMetadata(options, null);
            }
            else {
                final CloudBlockBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlockBlobReference(containerService.getKey(file));
                blob.setMetadata(new HashMap<String, String>(metadata));
                blob.uploadMetadata(null, options, null);
            }
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot write file attributes", e, file);
        }
    }
}
