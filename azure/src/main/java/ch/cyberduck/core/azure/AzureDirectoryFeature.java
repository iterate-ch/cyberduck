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

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.EnumSet;

import com.azure.core.exception.HttpResponseException;

public class AzureDirectoryFeature implements Directory<Void> {

    private final AzureSession session;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    private Write<Void> writer;

    public AzureDirectoryFeature(final AzureSession session) {
        this.session = session;
        this.writer = new AzureWriteFeature(session);
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(folder)) {
                // Container name must be lower case.
                session.getClient().getBlobContainerClient(containerService.getContainer(folder).getName()).create();
                return new Path(folder.getParent(), folder.getName(), folder.getType(), new AzureAttributesFinderFeature(session).find(folder));
            }
            else {
                final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
                type.add(Path.Type.placeholder);
                return new AzureTouchFeature(session).withWriter(writer).touch(folder.withType(type),
                        status.withChecksum(writer.checksum(folder, status).compute(new NullInputStream(0L), status)));
            }
        }
        catch(IllegalArgumentException e) {
            throw new InteroperabilityException();
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot()) {
            // Empty argument if not known in validation
            if(StringUtils.isNotBlank(filename)) {
                // Container names must be lowercase, between 3-63 characters long and must start with a letter or
                // number. Container names may contain only letters, numbers, and the dash (-) character.
                if(StringUtils.length(filename) > 63) {
                    throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename));
                }
                if(StringUtils.length(filename) < 3) {
                    throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename));
                }
                if(!StringUtils.isAlphanumeric(StringUtils.removeAll(filename, "-"))) {
                    throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename));
                }
            }
        }
    }

    @Override
    public Directory<Void> withWriter(final Write<Void> writer) {
        this.writer = writer;
        return this;
    }
}
