package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.storage.model.Bucket;

public class GoogleStorageDirectoryFeature implements Directory<VersionId> {

    private static final String MIMETYPE = "application/x-directory";

    private final PathContainerService containerService;
    private final GoogleStorageSession session;

    private Write<VersionId> writer;

    public GoogleStorageDirectoryFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
        this.writer = new GoogleStorageWriteFeature(session);
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(folder)) {
                final Bucket bucket = session.getClient().buckets().insert(session.getHost().getCredentials().getUsername(),
                    new Bucket()
                        .setLocation(status.getRegion())
                        .setStorageClass(status.getStorageClass())
                        .setName(containerService.getContainer(folder).getName())).execute();
                final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
                type.add(Path.Type.volume);
                return folder.withType(type).withAttributes(new GoogleStorageAttributesFinderFeature(session).toAttributes(bucket));
            }
            else {
                // Add placeholder object
                status.setMime(MIMETYPE);
                final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
                type.add(Path.Type.placeholder);
                final StatusOutputStream<VersionId> out = writer.write(new Path(folder.getParent(), folder.getName(), type,
                    new PathAttributes(folder.attributes())), status, new DisabledConnectionCallback());
                new DefaultStreamCloser().close(out);
                final VersioningConfiguration versioning = null != session.getFeature(Versioning.class) ? session.getFeature(Versioning.class).getConfiguration(
                    session.getFeature(PathContainerService.class).getContainer(folder)
                ) : VersioningConfiguration.empty();
                if(versioning.isEnabled()) {
                    return folder.withType(type).withAttributes(folder.attributes().withVersionId(out.getStatus().id));
                }
                return folder;
            }
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public Directory<VersionId> withWriter(final Write<VersionId> writer) {
        this.writer = writer;
        return this;
    }
}
