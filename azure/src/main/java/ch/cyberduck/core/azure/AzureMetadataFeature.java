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

import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

import com.azure.core.exception.HttpResponseException;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;

public class AzureMetadataFeature implements Headers {

    private final AzureSession session;

    private final PathContainerService containerService
        = new DirectoryDelimiterPathContainerService();

    public AzureMetadataFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getDefault(final Local local) {
        return new HostPreferences(session.getHost()).getMap("azure.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                return session.getClient().getBlobContainerClient(containerService.getContainer(file).getName()).getProperties().getMetadata();
            }
            else {
                final BlobClient client = session.getClient().getBlobContainerClient(containerService.getContainer(file).getName())
                        .getBlobClient(containerService.getKey(file));
                final BlobProperties properties = client.getProperties();
                final Map<String, String> metadata = properties.getMetadata();
                if(StringUtils.isNotBlank(properties.getContentType())) {
                    metadata.put(HttpHeaders.CONTENT_TYPE, properties.getContentType());
                }
                if(StringUtils.isNotBlank(properties.getCacheControl())) {
                    metadata.put(HttpHeaders.CACHE_CONTROL, properties.getCacheControl());
                }
                return metadata;
            }
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                session.getClient().getBlobContainerClient(containerService.getContainer(file).getName())
                        .setMetadata(new HashMap<>(status.getMetadata()));
            }
            else {
                final BlobClient client = session.getClient().getBlobContainerClient(containerService.getContainer(file).getName())
                        .getBlobClient(containerService.getKey(file));
                final HashMap<String, String> pruned = new HashMap<>();
                for(Map.Entry<String, String> m : status.getMetadata().entrySet()) {
                    if(HttpHeaders.CACHE_CONTROL.equalsIgnoreCase(m.getKey())) {
                        // Update properties
                        client.setHttpHeaders(new BlobHttpHeaders().setCacheControl(m.getValue()));
                        continue;
                    }
                    if(HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(m.getKey())) {
                        // Update properties
                        client.setHttpHeaders(new BlobHttpHeaders().setContentType(m.getValue()));
                        continue;
                    }
                    pruned.put(m.getKey(), m.getValue());
                }
                client.setMetadata(pruned);
            }
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
