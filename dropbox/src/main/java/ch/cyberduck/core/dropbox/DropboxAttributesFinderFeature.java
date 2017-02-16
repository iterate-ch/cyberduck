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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;

public class DropboxAttributesFinderFeature implements AttributesFinder {

    private final DropboxSession session;

    public DropboxAttributesFinderFeature(final DropboxSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        try {
            final Metadata metadata = new DbxUserFilesRequests(session.getClient()).getMetadata(file.getAbsolute());
            return this.convert(metadata);
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    protected PathAttributes convert(final Metadata metadata) {
        final PathAttributes attributes = new PathAttributes();
        if(metadata instanceof FileMetadata) {
            final FileMetadata fm = (FileMetadata) metadata;
            attributes.setSize(fm.getSize());
            attributes.setVersionId(fm.getId());
            attributes.setModificationDate(fm.getClientModified().getTime());
        }
        else if(metadata instanceof FolderMetadata) {
            final FolderMetadata fm = (FolderMetadata) metadata;
            attributes.setVersionId(fm.getId());
        }
        return attributes;
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        return this;
    }
}
