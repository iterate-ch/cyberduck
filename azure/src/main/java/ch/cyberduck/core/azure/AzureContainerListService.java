package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.util.EnumSet;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.ContainerListingDetails;

public class AzureContainerListService implements RootListService {

    private final AzureSession session;

    private final OperationContext context;

    private final Preferences preferences
            = PreferencesFactory.get();

    public AzureContainerListService(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        ResultSegment<CloudBlobContainer> result;
        ResultContinuation token = null;
        try {
            final AttributedList<Path> containers = new AttributedList<Path>();
            do {
                final BlobRequestOptions options = new BlobRequestOptions();
                options.setRetryPolicyFactory(new RetryNoRetry());
                result = session.getClient().listContainersSegmented(null, ContainerListingDetails.NONE,
                        preferences.getInteger("azure.listing.chunksize"), token,
                        options, context);
                for(CloudBlobContainer container : result.getResults()) {
                    final PathAttributes attributes = new PathAttributes();
                    attributes.setETag(container.getProperties().getEtag());
                    attributes.setModificationDate(container.getProperties().getLastModified().getTime());
                    containers.add(new Path(String.format("/%s", container.getName()),
                            EnumSet.of(Path.Type.volume, Path.Type.directory), attributes));
                }
                listener.chunk(directory, containers);
                token = result.getContinuationToken();
            }
            while(result.getHasMoreResults());
            return containers;
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
