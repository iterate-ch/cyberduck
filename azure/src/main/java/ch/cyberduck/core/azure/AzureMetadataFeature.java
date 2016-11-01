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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public class AzureMetadataFeature implements Headers {

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
            = new AzurePathContainerService();

    public AzureMetadataFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public Map<String, String> getDefault(final Local local) {
        return PreferencesFactory.get().getMap("azure.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                container.downloadAttributes();
                return container.getMetadata();
            }
            else {
                final CloudBlockBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlockBlobReference(containerService.getKey(file));
                // Populates the blob properties and metadata
                blob.downloadAttributes(null, null, context);
                final Map<String, String> metadata = new HashMap<String, String>();
                metadata.putAll(blob.getMetadata());
                final BlobProperties properties = blob.getProperties();
                if(StringUtils.isNotBlank(properties.getCacheControl())) {
                    metadata.put(HttpHeaders.CACHE_CONTROL, properties.getCacheControl());
                }
                if(StringUtils.isNotBlank(properties.getContentType())) {
                    metadata.put(HttpHeaders.CONTENT_TYPE, properties.getContentType());
                }
                return metadata;
            }
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        try {
            final BlobRequestOptions options = new BlobRequestOptions();
            if(containerService.isContainer(file)) {
                final CloudBlobContainer container = session.getClient().getContainerReference(containerService.getContainer(file).getName());
                container.setMetadata(new HashMap<String, String>(metadata));
                container.uploadMetadata(AccessCondition.generateEmptyCondition(), options, context);
            }
            else {
                final CloudBlockBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlockBlobReference(containerService.getKey(file));
                // Populates the blob properties and metadata
                blob.downloadAttributes();
                // Replace metadata
                final HashMap<String, String> pruned = new HashMap<String, String>();
                for(Map.Entry<String, String> m : metadata.entrySet()) {
                    final BlobProperties properties = blob.getProperties();
                    if(HttpHeaders.CACHE_CONTROL.equalsIgnoreCase(m.getKey())) {
                        // Update properties
                        properties.setCacheControl(m.getValue());
                        continue;
                    }
                    if(HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(m.getKey())) {
                        // Update properties
                        properties.setContentType(m.getValue());
                        continue;
                    }
                    pruned.put(m.getKey(), m.getValue());
                }
                blob.setMetadata(pruned);
                blob.uploadMetadata(AccessCondition.generateEmptyCondition(), options, context);
                blob.uploadProperties();
            }
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
