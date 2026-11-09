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

import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Optional;


public class AzureDirectoryFeature implements Directory<Void> {

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    public AzureDirectoryFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Write<Void> writer, final Path folder, final TransferStatus status) throws BackgroundException {
        if(containerService.isContainer(folder)) {
            try {
                // Container name must be lower case.
                session.getClient().getBlobServiceClient().getBlobContainerClient(containerService.getContainer(folder).getName()).create();
                return new Path(folder.getParent(), folder.getName(), folder.getType());
            }
            catch(RuntimeException e) {
                throw new AzureExceptionMappingService().map("Cannot create folder {0}", e, folder);
            }
        }
        if(session.getStorageAccountInfo().isHierarchicalNamespaceEnabled()) {
            return new AzureDataLakeDirectoryFeature(session).mkdir(writer, folder, status);
        }
        return new AzureBlobDirectoryFeature(session).mkdir(writer, folder, status);
    }

    @Override
    public void preflight(final Path workdir, final Optional<String> filename) throws BackgroundException {
        if(workdir.isRoot()) {
            if(filename.isPresent()) {
                if(!validate(filename.get())) {
                    throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename));
                }
            }
            // Empty argument if not known in validation
        }
    }

    public static boolean validate(final String filename) {
        // Container names must be lowercase, between 3-63 characters long and must start with a letter or
        // number. Container names may contain only letters, numbers, and the dash (-) character.
        if(StringUtils.length(filename) > 63) {
            return false;
        }
        if(StringUtils.length(filename) < 3) {
            return false;
        }
        if(!StringUtils.isAlphanumeric(RegExUtils.removeAll(filename, "-"))) {
            return false;
        }
        return true;
    }
}
