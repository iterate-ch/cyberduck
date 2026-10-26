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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;


/**
 * Create directories with Data Lake Storage API for accounts with hierarchical namespace enabled
 */
public class AzureDataLakeDirectoryFeature implements Directory<Void> {

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    public AzureDataLakeDirectoryFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Write<Void> writer, final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            session.getClient().getDataLakeServiceClient().getFileSystemClient(containerService.getContainer(folder).getName())
                    .createDirectory(StringUtils.removeEnd(containerService.getKey(folder), String.valueOf(Path.DELIMITER)));
            return new Path(folder.getParent(), folder.getName(), folder.getType());
        }
        catch(IllegalArgumentException e) {
            throw new InteroperabilityException();
        }
        catch(RuntimeException e) {
            throw new AzureExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }
}
