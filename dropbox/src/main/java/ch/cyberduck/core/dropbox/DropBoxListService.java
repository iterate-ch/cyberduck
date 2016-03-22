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
import ch.cyberduck.core.io.Checksum;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.EnumSet;
import java.util.List;

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
            String path = directory.isRoot() ? "" : directory.getAbsolute();
            ListFolderResult result = session.getClient().getDbxClient().files().listFolder(path);

            List<Metadata> q = result.getEntries();
            for (Metadata md : result.getEntries()) {
                final PathAttributes attributes = new PathAttributes();
                //attributes.setSize(md. f.getSize());
                //attributes.setVersionId(f.getId());
                //attributes.setModificationDate(f.getModifiedTime().getValue());
                //attributes.setCreationDate(f.getCreatedTime().getValue());
                //attributes.setChecksum(Checksum.parse(f.getMd5Checksum()));
                final Path child = new Path(directory, PathNormalizer.name(md.getName()), EnumSet.of(Path.Type.file), attributes);
                children.add(child);
            }
            return children;
        } catch(DbxException e){
            e.printStackTrace();
            return null;
        }
    }
}
