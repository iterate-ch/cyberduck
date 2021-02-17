package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

import com.azure.core.exception.HttpResponseException;

public class AzureDirectoryFeature implements Directory<Void> {

    private final PathContainerService containerService
        = new AzurePathContainerService();

    private final AzureSession session;

    private Write<Void> writer;

    public AzureDirectoryFeature(final AzureSession session) {
        this.session = session;
        this.writer = new AzureWriteFeature(session);
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(folder)) {
                // Container name must be lower case.
                session.getClient().getBlobContainerClient(containerService.getContainer(folder).getName()).create();
                return new Path(folder.getParent(), folder.getName(), folder.getType(), new AzureAttributesFinderFeature(session).find(folder));
            }
            else {
                status.setChecksum(writer.checksum(folder, status).compute(new NullInputStream(0L), status));
                final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
                type.add(Path.Type.placeholder);
                final Path placeholder = new Path(folder.getParent(), folder.getName(), type,
                    new PathAttributes(folder.attributes()));
                new DefaultStreamCloser().close(writer.write(placeholder, status, new DisabledConnectionCallback()));
                return new Path(placeholder.getParent(), placeholder.getName(), placeholder.getType(), new AzureAttributesFinderFeature(session).find(placeholder));
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
    public boolean isSupported(final Path workdir, final String name) {
        if(workdir.isRoot()) {
            // Empty argument if not known in validation
            if(StringUtils.isNotBlank(name)) {
                // Container names must be lowercase, between 3-63 characters long and must start with a letter or
                // number. Container names may contain only letters, numbers, and the dash (-) character.
                if(StringUtils.length(name) > 63) {
                    return false;
                }
                if(StringUtils.length(name) < 3) {
                    return false;
                }
                return StringUtils.isAlphanumeric(StringUtils.removeAll(name, "-"));
            }
        }
        return true;
    }

    @Override
    public Directory<Void> withWriter(final Write<Void> writer) {
        this.writer = writer;
        return this;
    }
}
