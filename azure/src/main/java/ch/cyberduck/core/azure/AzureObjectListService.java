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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;

public class AzureObjectListService implements ListService {
    private static final Logger log = LogManager.getLogger(AzureObjectListService.class);

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();
    private final AzureAttributesFinderFeature attributes;

    public AzureObjectListService(final AzureSession session) {
        this.session = session;
        this.attributes = new AzureAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            final BlobContainerClient containerClient = session.getClient().getBlobServiceClient()
                    .getBlobContainerClient(containerService.getContainer(directory).getName());
            String prefix = StringUtils.EMPTY;
            if(!containerService.isContainer(directory)) {
                prefix = containerService.getKey(directory);
                if(!prefix.endsWith(String.valueOf(Path.DELIMITER))) {
                    prefix += Path.DELIMITER;
                }
            }
            boolean hasDirectoryPlaceholder = containerService.isContainer(directory);
            final int chunksize = HostPreferencesFactory.get(session.getHost()).getInteger("azure.listing.chunksize");
            String continuationToken = null;
            for(PagedResponse<BlobItem> response : containerClient.listBlobsByHierarchy(String.valueOf(Path.DELIMITER), new ListBlobsOptions()
                    .setDetails(new BlobListDetails().setRetrieveMetadata(true))
                    .setPrefix(prefix)
                    .setMaxResultsPerPage(chunksize), null).iterableByPage(continuationToken, chunksize)) {
                for(BlobItem item : response.getElements()) {
                    if(StringUtils.equals(prefix, item.getName())) {
                        if(log.isDebugEnabled()) {
                            log.debug("Skip placeholder key {}", item);
                        }
                        hasDirectoryPlaceholder = true;
                        continue;
                    }
                    final PathAttributes attr;
                    if(item.isPrefix()) {
                        attr = PathAttributes.EMPTY;
                    }
                    else {
                        attr = attributes.toAttributes(item.getProperties());
                    }
                    // A directory is designated by a delimiter character.
                    final EnumSet<Path.Type> types = null != item.isPrefix() && item.isPrefix()
                            ? EnumSet.of(Path.Type.directory, Path.Type.placeholder) : EnumSet.of(Path.Type.file);
                    children.add(new Path(directory, PathNormalizer.name(item.getName()), types, attr));
                }
                listener.chunk(directory, children);
                continuationToken = response.getContinuationToken();
                if(StringUtils.isBlank(continuationToken)) {
                    break;
                }
            }
            if(!hasDirectoryPlaceholder && children.isEmpty()) {
                if(log.isWarnEnabled()) {
                    log.warn("No placeholder found for directory {}", directory);
                }
                throw new NotfoundException(directory.getAbsolute());
            }
            return children;
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
