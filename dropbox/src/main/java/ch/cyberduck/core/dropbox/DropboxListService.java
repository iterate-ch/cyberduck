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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.EnumSet;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

public class DropboxListService implements ListService {
    private static final Logger log = Logger.getLogger(DropboxListService.class);

    private final DropboxSession session;

    private final DropboxAttributesFinderFeature attributes;

    public DropboxListService(final DropboxSession session) {
        this.session = session;
        this.attributes = new DropboxAttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            final String path = directory.isRoot() ? StringUtils.EMPTY : directory.getAbsolute();
            ListFolderResult result;
            this.parse(directory, listener, children, result = new DbxUserFilesRequests(session.getClient()).listFolder(path));
            // If true, then there are more entries available. Pass the cursor to list_folder/continue to retrieve the rest.
            while(result.getHasMore()) {
                this.parse(directory, listener, children, result = new DbxUserFilesRequests(session.getClient())
                        .listFolderContinue(result.getCursor()));
            }
            return children;
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map(e);
        }
    }

    private void parse(final Path directory, final ListProgressListener listener, final AttributedList<Path> children, final ListFolderResult result)
            throws ConnectionCanceledException {
        for(Metadata md : result.getEntries()) {
            final Path child = this.parse(directory, md);
            if(child == null) {
                continue;
            }
            children.add(child);
            listener.chunk(directory, children);
        }
    }

    protected Path parse(final Path directory, final Metadata metadata) {
        final EnumSet<AbstractPath.Type> type;
        if(metadata instanceof FileMetadata) {
            type = EnumSet.of(Path.Type.file);
        }
        else if(metadata instanceof FolderMetadata) {
            type = EnumSet.of(Path.Type.directory);
        }
        else {
            log.warn(String.format("Skip file %s", metadata));
            return null;
        }
        return new Path(directory, PathNormalizer.name(metadata.getName()), type, attributes.convert(metadata));
    }
}
