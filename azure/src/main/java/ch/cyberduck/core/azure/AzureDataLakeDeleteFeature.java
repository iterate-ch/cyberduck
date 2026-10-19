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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;

/**
 * Delete with Data Lake Storage API for accounts with hierarchical namespace enabled
 */
public class AzureDataLakeDeleteFeature implements Delete {

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    public AzureDataLakeDeleteFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> containers = new ArrayList<>();
        for(Path file : files.keySet()) {
            if(containerService.isContainer(file)) {
                containers.add(file);
            }
            else {
                callback.delete(file);
                try {
                    // Directory is not addressable with trailing delimiter in hierarchical namespace
                    final String key = StringUtils.removeEnd(containerService.getKey(file), String.valueOf(Path.DELIMITER));
                    if(file.isDirectory()) {
                        session.getClient().getDataLakeServiceClient().getFileSystemClient(containerService.getContainer(file).getName())
                                .deleteDirectoryWithResponse(key, true, null, null, Context.NONE);
                    }
                    else {
                        session.getClient().getDataLakeServiceClient().getFileSystemClient(containerService.getContainer(file).getName())
                                .deleteFile(key);
                    }
                }
                catch(HttpResponseException e) {
                    throw new AzureExceptionMappingService().map("Cannot delete {0}", e, file);
                }
            }
        }
        for(Path file : containers) {
            callback.delete(file);
            try {
                session.getClient().getDataLakeServiceClient().getFileSystemClient(containerService.getContainer(file).getName()).delete();
            }
            catch(HttpResponseException e) {
                throw new AzureExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.recursive);
    }
}
