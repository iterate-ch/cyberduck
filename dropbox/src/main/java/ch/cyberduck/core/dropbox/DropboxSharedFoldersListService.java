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
 * but WITHOUT ANYdrivelexico WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.sharing.DbxUserSharingRequests;
import com.dropbox.core.v2.sharing.ListFoldersResult;
import com.dropbox.core.v2.sharing.SharedFolderMetadata;

public class DropboxSharedFoldersListService implements ListService {
    private static final Logger log = LogManager.getLogger(DropboxSharedFoldersListService.class);

    private final DropboxSession session;

    public DropboxSharedFoldersListService(final DropboxSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            ListFoldersResult listFoldersResult;
            this.parse(directory, listener, children, listFoldersResult = new DbxUserSharingRequests(session.getClient()).listFolders());
            while(listFoldersResult.getCursor() != null) {
                this.parse(directory, listener, children, listFoldersResult = new DbxUserSharingRequests(session.getClient())
                    .listMountableFoldersContinue(listFoldersResult.getCursor()));
            }
            return children;
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }

    private void parse(final Path directory, final ListProgressListener listener, final AttributedList<Path> children, final ListFoldersResult result) throws ConnectionCanceledException {
        for(SharedFolderMetadata metadata : result.getEntries()) {
            final Path f = this.parse(directory, metadata);
            // Reference team folder
            f.attributes().withFileId(metadata.getSharedFolderId());
            children.add(f);
            listener.chunk(directory, children);
        }
    }

    protected Path parse(final Path directory, final SharedFolderMetadata metadata) {
        final PathAttributes attr = new PathAttributes();
        return new Path(directory, PathNormalizer.name(metadata.getName()),
            EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.shared), attr);
    }
}
