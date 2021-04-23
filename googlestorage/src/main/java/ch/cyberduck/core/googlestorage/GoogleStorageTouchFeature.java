package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

public class GoogleStorageTouchFeature implements Touch<VersionId> {

    private Write<VersionId> writer;
    private final GoogleStorageSession session;

    public GoogleStorageTouchFeature(final GoogleStorageSession session) {
        this.writer = new GoogleStorageWriteFeature(session);
        this.session = session;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        status.setLength(0L);
        final StatusOutputStream<VersionId> out = writer.write(file, status, new DisabledConnectionCallback());
        new DefaultStreamCloser().close(out);
        final VersioningConfiguration versioning = null != session.getFeature(Versioning.class) ? session.getFeature(Versioning.class).getConfiguration(
            session.getFeature(PathContainerService.class).getContainer(file)
        ) : VersioningConfiguration.empty();
        if(versioning.isEnabled()) {
            return file.withAttributes(new PathAttributes(file.attributes()).withVersionId(out.getStatus().id));
        }
        return file;
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }

    @Override
    public Touch<VersionId> withWriter(final Write<VersionId> writer) {
        this.writer = writer;
        return this;
    }
}
