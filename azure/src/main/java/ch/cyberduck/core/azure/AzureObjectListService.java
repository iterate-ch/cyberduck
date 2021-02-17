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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobsOptions;

public class AzureObjectListService implements ListService {

    private final AzureSession session;

    private final PathContainerService containerService
        = new AzurePathContainerService();

    public AzureObjectListService(final AzureSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final BlobContainerClient containerClient = session.getClient().getBlobContainerClient(containerService.getContainer(directory).getName());
            final AttributedList<Path> children = new AttributedList<Path>();
            PagedIterable<BlobItem> result;
            String prefix = StringUtils.EMPTY;
            if(!containerService.isContainer(directory)) {
                prefix = containerService.getKey(directory);
                if(!prefix.endsWith(String.valueOf(Path.DELIMITER))) {
                    prefix += Path.DELIMITER;
                }
            }
            boolean hasDirectoryPlaceholder = containerService.isContainer(directory);
            String continuationToken = null;
            for(PagedResponse<BlobItem> response : containerClient.listBlobsByHierarchy(String.valueOf(Path.DELIMITER), new ListBlobsOptions()
                .setDetails(new BlobListDetails()
                    .setRetrieveMetadata(true))
                .setPrefix(prefix)
                .setMaxResultsPerPage(PreferencesFactory.get().getInteger("azure.listing.chunksize")), null).iterableByPage(continuationToken,
                PreferencesFactory.get().getInteger("azure.listing.chunksize"))) {
                for(BlobItem item : response.getElements()) {
                    if(StringUtils.equals(prefix, item.getName())) {
                        hasDirectoryPlaceholder = true;
                        continue;
                    }
                    final PathAttributes attributes;
                    if(null == item.getProperties()) {
                        attributes = PathAttributes.EMPTY;
                    }
                    else {
                        attributes = new AzureAttributesFinderFeature(session).toAttributes(item.getProperties());
                    }
                    // A directory is designated by a delimiter character.
                    final EnumSet<Path.Type> types = null != item.isPrefix() && item.isPrefix()
                        ? EnumSet.of(Path.Type.directory, Path.Type.placeholder) : EnumSet.of(Path.Type.file);
                    final Path child = new Path(directory, PathNormalizer.name(item.getName()), types, attributes);
                    children.add(child);
                    listener.chunk(directory, children);
                }
                continuationToken = response.getContinuationToken();
                if(StringUtils.isBlank(continuationToken)) {
                    break;
                }
            }
            if(!hasDirectoryPlaceholder && children.isEmpty()) {
                throw new NotfoundException(directory.getAbsolute());
            }
            return children;
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
