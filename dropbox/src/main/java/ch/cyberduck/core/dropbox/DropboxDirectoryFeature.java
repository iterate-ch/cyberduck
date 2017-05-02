package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.serializer.PathAttributesDictionary;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FolderMetadata;

public class DropboxDirectoryFeature implements Directory<Void> {

    private final DropboxSession session;

    public DropboxDirectoryFeature(final DropboxSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        try {
            final FolderMetadata metadata = new DbxUserFilesRequests(session.getClient()).createFolder(folder.getAbsolute());
            final Path p = new Path(folder.getParent(), folder.getName(), EnumSet.of(Path.Type.directory),
                    new PathAttributesDictionary().deserialize(folder.attributes().serialize(SerializerFactory.get())));
            p.attributes().setVersionId(metadata.getId());
            return p;
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }

    @Override
    public DropboxDirectoryFeature withWriter(final Write<Void> writer) {
        return this;
    }
}
