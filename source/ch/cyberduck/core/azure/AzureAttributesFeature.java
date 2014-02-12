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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Attributes;

import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.blob.client.BlobContainerProperties;
import com.microsoft.windowsazure.services.blob.client.BlobProperties;
import com.microsoft.windowsazure.services.blob.client.BlobRequestOptions;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id$
 */
public class AzureAttributesFeature implements Attributes {

    private AzureSession session;

    private PathContainerService containerService
            = new PathContainerService();

    public AzureAttributesFeature(AzureSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final PathAttributes attributes = new PathAttributes(Path.DIRECTORY_TYPE | Path.VOLUME_TYPE);
                final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                container.downloadAttributes();
                final BlobContainerProperties properties = container.getProperties();
                attributes.setETag(properties.getEtag());
                attributes.setModificationDate(properties.getLastModified().getTime());
                return attributes;
            }
            else {
                final CloudBlockBlob blob;
                if(file.attributes().isPlaceholder()) {
                    blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                            .getBlockBlobReference(containerService.getKey(file).concat(String.valueOf(Path.DELIMITER)));
                }
                else {
                    blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                            .getBlockBlobReference(containerService.getKey(file));
                }
                final BlobRequestOptions options = new BlobRequestOptions();
                options.setRetryPolicyFactory(new RetryNoRetry());
                blob.downloadAttributes(null, options, null);
                final BlobProperties properties = blob.getProperties();
                final PathAttributes attributes = new PathAttributes(
                        "application/directory".equals(properties.getContentType()) ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                attributes.setSize(properties.getLength());
                attributes.setModificationDate(properties.getLastModified().getTime());
                attributes.setChecksum(properties.getContentMD5());
                attributes.setETag(properties.getEtag());
                return attributes;
            }
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot read file attributes", e, file);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }

    @Override
    public Attributes withCache(Cache cache) {
        return this;
    }
}
