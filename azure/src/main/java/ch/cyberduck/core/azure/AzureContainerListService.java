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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;

import java.util.EnumSet;

import com.azure.core.exception.HttpResponseException;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.ListBlobContainersOptions;

public class AzureContainerListService implements RootListService {

    private final AzureSession session;

    public AzureContainerListService(final AzureSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> containers = new AttributedList<>();
            for(BlobContainerItem container : session.getClient().listBlobContainers(new ListBlobContainersOptions()
                    .setMaxResultsPerPage(new HostPreferences(session.getHost()).getInteger("azure.listing.chunksize"))
                    .setDetails(new BlobContainerListDetails().setRetrieveDeleted(false).setRetrieveMetadata(true)), null)) {
                final PathAttributes attributes = new PathAttributes();
                attributes.setETag(container.getProperties().getETag());
                attributes.setModificationDate(container.getProperties().getLastModified().toInstant().toEpochMilli());
                containers.add(new Path(PathNormalizer.normalize(container.getName()), EnumSet.of(Path.Type.volume, Path.Type.directory), attributes));
                listener.chunk(directory, containers);
            }
            return containers;
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
