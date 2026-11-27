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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.shared.MetadataSizeFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;

public class AzureMetadataFeature implements Metadata {

    /**
     * Maximum number of bytes of names and values of all metadata
     */
    public static final int MAXIMUM_TOTAL_SIZE = 8192;

    private final AzureSession session;

    private final PathContainerService containerService
        = new DirectoryDelimiterPathContainerService();

    public AzureMetadataFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getDefault(final Path file) {
        final Map<String, String> metadata = HostPreferencesFactory.get(session.getHost()).getMap("azure.metadata.default");
        if(metadata.isEmpty()) {
            return Metadata.EMPTY;
        }
        return metadata;
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                return session.getClient().getBlobServiceClient().getBlobContainerClient(containerService.getContainer(file).getName()).getProperties().getMetadata();
            }
            else {
                final BlobClient client = session.getClient().getBlobServiceClient().getBlobContainerClient(containerService.getContainer(file).getName())
                        .getBlobClient(containerService.getKey(file));
                final BlobProperties properties = client.getProperties();
                return AzureBlobAttributesFinderFeature.toMetadata(properties.getMetadata(), properties.getContentType(), properties.getCacheControl());
            }
        }
        catch(RuntimeException e) {
            throw new AzureExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                session.getClient().getBlobServiceClient().getBlobContainerClient(containerService.getContainer(file).getName())
                        .setMetadata(filter(file, status.getMetadata()));
            }
            else {
                final BlobClient client = session.getClient().getBlobServiceClient().getBlobContainerClient(containerService.getContainer(file).getName())
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
                client.setMetadata(filter(file, pruned));
            }
        }
        catch(RuntimeException e) {
            throw new AzureExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    /**
     * @param file     File
     * @param metadata Metadata to write
     * @return Metadata with entries exceeding limits removed. Properties set as HTTP headers are not subject to limits
     */
    public static Map<String, String> filter(final Path file, final Map<String, String> metadata) {
        return new MetadataSizeFilter()
                .withMaximumTotalSize(MAXIMUM_TOTAL_SIZE)
                .withExcluded(key -> StringUtils.equalsAnyIgnoreCase(key, HttpHeaders.CACHE_CONTROL, HttpHeaders.CONTENT_TYPE))
                .filter(file, metadata);
    }
}
