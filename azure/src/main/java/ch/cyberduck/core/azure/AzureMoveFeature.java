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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import com.azure.core.exception.HttpResponseException;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;

public class AzureMoveFeature implements Move {

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();
    private final AzureCopyFeature proxy;
    private final AzureDeleteFeature delete;

    public AzureMoveFeature(final AzureSession session) {
        this.session = session;
        this.proxy = new AzureCopyFeature(session);
        this.delete = new AzureDeleteFeature(session);
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        proxy.preflight(source, target);
        delete.preflight(source);
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        if(session.getStorageAccountInfo().isHierarchicalNamespaceEnabled()) {
            // Atomic rename supported for files and directories
            try {
                final DataLakeFileSystemClient filesystem = session.getClient().getDataLakeServiceClient()
                        .getFileSystemClient(containerService.getContainer(file).getName());
                final String source = StringUtils.removeEnd(containerService.getKey(file), String.valueOf(Path.DELIMITER));
                final String target = StringUtils.removeEnd(containerService.getKey(renamed), String.valueOf(Path.DELIMITER));
                final String container = containerService.getContainer(renamed).getName();
                if(file.isDirectory()) {
                    filesystem.getDirectoryClient(source).rename(container, target);
                }
                else {
                    filesystem.getFileClient(source).rename(container, target);
                }
                return new Path(renamed).withAttributes(file.attributes());
            }
            catch(HttpResponseException e) {
                throw new AzureExceptionMappingService().map("Cannot rename {0}", e, file);
            }
        }
        else {
            final Path copy = proxy.copy(file, renamed, status, connectionCallback, StreamListener.noop);
            delete.delete(Collections.singletonList(file), connectionCallback, callback);
            return copy;
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        if(session.getStorageAccountInfo().isHierarchicalNamespaceEnabled()) {
            return EnumSet.of(Flags.recursive);
        }
        return EnumSet.noneOf(Flags.class);
    }
}
