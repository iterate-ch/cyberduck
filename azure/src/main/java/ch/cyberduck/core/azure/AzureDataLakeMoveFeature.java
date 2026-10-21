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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import com.azure.storage.file.datalake.DataLakeFileSystemClient;

/**
 * Atomic rename of files and directories with Data Lake Storage API for accounts with hierarchical namespace enabled
 */
public class AzureDataLakeMoveFeature implements Move {
    private static final Logger log = LogManager.getLogger(AzureDataLakeMoveFeature.class);

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    public AzureDataLakeMoveFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        if(containerService.isContainer(source)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
        if(target.isPresent()) {
            if(containerService.isContainer(target.get())) {
                throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
            }
        }
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            final DataLakeFileSystemClient filesystem = session.getClient().getDataLakeServiceClient()
                    .getFileSystemClient(containerService.getContainer(file).getName());
            final String source = StringUtils.removeEnd(containerService.getKey(file), String.valueOf(Path.DELIMITER));
            final String target = StringUtils.removeEnd(containerService.getKey(renamed), String.valueOf(Path.DELIMITER));
            final String container = containerService.getContainer(renamed).getName();
            if(file.isDirectory()) {
                if(status.isExists()) {
                    // Rename of a file replaces the target but a directory cannot be overwritten (409 Conflict)
                    log.warn("Delete directory {} to be replaced with {}", renamed, file);
                    new AzureDataLakeDeleteFeature(session).delete(Collections.singletonList(renamed), connectionCallback, callback);
                }
                filesystem.getDirectoryClient(source).rename(container, target);
            }
            else {
                filesystem.getFileClient(source).rename(container, target);
            }
            return new Path(renamed).withAttributes(file.attributes());
        }
        catch(RuntimeException e) {
            throw new AzureExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }
}
