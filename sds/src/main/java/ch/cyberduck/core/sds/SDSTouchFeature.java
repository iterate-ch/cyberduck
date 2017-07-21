package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

public class SDSTouchFeature implements Touch<VersionId> {

    private Write<VersionId> writer;

    public SDSTouchFeature(final SDSSession session) {
        this.writer = new SDSWriteFeature(session);
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        final StatusOutputStream<VersionId> out = writer.write(file, status, new DisabledConnectionCallback());
        try {
            out.close();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create file {0}", e, file);
        }
        return new Path(file.getParent(), file.getName(), file.getType(),
                new PathAttributes(file.attributes()).withVersionId(out.getStatus().toString()));
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return !workdir.isRoot();
    }

    @Override
    public Touch<VersionId> withWriter(final Write<VersionId> writer) {
        this.writer = writer;
        return this;
    }
}
