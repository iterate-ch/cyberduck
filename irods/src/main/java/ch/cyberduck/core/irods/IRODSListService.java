package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.commons.lang3.StringUtils;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.CollectionEntry;
import org.irods.irods4j.high_level.vfs.IRODSCollectionIterator;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.EnumSet;

public class IRODSListService implements ListService {

    private final IRODSSession session;

    public IRODSListService(IRODSSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final IRODSConnection conn = session.getClient();

            String logicalPath = directory.getAbsolute();
            if(!IRODSFilesystem.exists(conn.getRcComm(), logicalPath)) {
                throw new NotfoundException(logicalPath);
            }

            final AttributedList<Path> children = new AttributedList<Path>();

            for(CollectionEntry entry : new IRODSCollectionIterator(conn.getRcComm(), logicalPath)) {
                final String normalized = PathNormalizer.normalize(entry.path(), true);
                if(StringUtils.equals(normalized, directory.getAbsolute())) {
                    continue;
                }

                PathAttributes attrs = new DefaultPathAttributes();
                attrs.setCreationDate(entry.createdAt() * 1000L);
                attrs.setModificationDate(entry.modifiedAt() * 1000L);

                EnumSet<Path.Type> type = EnumSet.of(Path.Type.file);

                if(entry.isCollection()) {
                    attrs.setDirectoryId(entry.id());
                    type = EnumSet.of(Path.Type.directory);
                }
                else if(entry.isDataObject()) {
                    attrs.setFileId(entry.id());
                    attrs.setSize(entry.dataSize());
                    attrs.setChecksum(IRODSChecksumUtils.toChecksum(entry.checksum()));
                }

                children.add(new Path(directory, PathNormalizer.name(normalized), type, attrs));
                listener.chunk(directory, children);
            }

            return children;
        }
        catch(IRODSException e) {
            throw new IRODSExceptionMappingService().map("Listing {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Listing {0} failed", e, directory);
        }
    }
}
