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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.EnumSet;

public class DropBoxListService implements ListService {
    private static final Logger log = Logger.getLogger(DropBoxListService.class);

    private final DropBoxSession session;

    public DropBoxListService(DropBoxSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(Path directory, ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            final String path = directory.isRoot() ? StringUtils.EMPTY : directory.getAbsolute();
            ListFolderResult result = session.getClient().getDbxClient().files().listFolder(path);
            for (Metadata md : result.getEntries()) {
                final PathAttributes attributes = new PathAttributes();
                if (md instanceof FileMetadata) {
                    FileMetadata fmd = (FileMetadata)md;
                    attributes.setSize(fmd.getSize());
                    attributes.setVersionId(fmd.getId());
                    attributes.setModificationDate(fmd.getClientModified().getTime());
                    //attributes.setCreationDate();
                    //attributes.setChecksum();
                }

                final Path child = new Path(directory, PathNormalizer.name(md.getName()), EnumSet.of(Path.Type.file), attributes);
                children.add(child);
            }
            return children;
        } catch (DbxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
