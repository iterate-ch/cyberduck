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

import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import com.azure.core.exception.HttpResponseException;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.models.PathProperties;

/**
 * Find with Data Lake Storage API for accounts with hierarchical namespace enabled. Files and directories share
 * a single namespace and a match requires the expected type.
 */
public class AzureDataLakeFindFeature implements Find {

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    public AzureDataLakeFindFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            final DataLakeFileSystemClient filesystem = session.getClient().getDataLakeServiceClient()
                    .getFileSystemClient(containerService.getContainer(file).getName());
            if(containerService.isContainer(file)) {
                return filesystem.exists();
            }
            final PathProperties properties = filesystem
                    .getFileClient(StringUtils.removeEnd(containerService.getKey(file), String.valueOf(Path.DELIMITER)))
                    .getProperties();
            return properties.isDirectory() == file.isDirectory();
        }
        catch(HttpResponseException e) {
            if(HttpStatus.SC_NOT_FOUND == e.getResponse().getStatusCode()) {
                return false;
            }
            throw new AzureExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }
}
