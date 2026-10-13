package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathItem;

/**
 * List with Data Lake Storage API for accounts with hierarchical namespace enabled. Directories are reported by
 * the service and a missing directory fails with not found.
 */
public class AzureDataLakeListService implements ListService {

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();
    private final AzureDataLakeAttributesFinderFeature attributes;

    public AzureDataLakeListService(final AzureSession session) {
        this.session = session;
        this.attributes = new AzureDataLakeAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            final DataLakeFileSystemClient filesystem = session.getClient().getDataLakeServiceClient()
                    .getFileSystemClient(containerService.getContainer(directory).getName());
            final ListPathsOptions options = new ListPathsOptions().setRecursive(false);
            if(!containerService.isContainer(directory)) {
                options.setPath(StringUtils.removeEnd(containerService.getKey(directory), String.valueOf(Path.DELIMITER)));
            }
            final int chunksize = HostPreferencesFactory.get(session.getHost()).getInteger("azure.listing.chunksize");
            String continuationToken = null;
            for(PagedResponse<PathItem> response : filesystem.listPaths(options, null).iterableByPage(continuationToken, chunksize)) {
                for(PathItem item : response.getElements()) {
                    children.add(new Path(directory, PathNormalizer.name(item.getName()),
                            item.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file), attributes.toAttributes(item)));
                }
                listener.chunk(directory, children);
                continuationToken = response.getContinuationToken();
                if(StringUtils.isBlank(continuationToken)) {
                    break;
                }
            }
            return children;
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
