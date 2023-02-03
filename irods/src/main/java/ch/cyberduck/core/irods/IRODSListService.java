package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;

import java.io.File;
import java.util.EnumSet;

public class IRODSListService implements ListService {

    private final IRODSSession session;

    public IRODSListService(IRODSSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            final IRODSFileSystemAO fs = session.getClient();
            final IRODSFile f = fs.getIRODSFileFactory().instanceIRODSFile(directory.getAbsolute());
            if(!f.exists()) {
                throw new NotfoundException(directory.getAbsolute());
            }
            for(File file : fs.getListInDirWithFileFilter(f, TrueFileFilter.TRUE)) {
                final String normalized = PathNormalizer.normalize(file.getAbsolutePath(), true);
                if(StringUtils.equals(normalized, directory.getAbsolute())) {
                    continue;
                }
                final PathAttributes attributes = new PathAttributes();
                final ObjStat stats = fs.getObjStat(file.getAbsolutePath());
                attributes.setModificationDate(stats.getModifiedAt().getTime());
                attributes.setCreationDate(stats.getCreatedAt().getTime());
                attributes.setSize(stats.getObjSize());
                attributes.setChecksum(Checksum.parse(Hex.encodeHexString(Base64.decodeBase64(stats.getChecksum()))));
                attributes.setOwner(stats.getOwnerName());
                attributes.setGroup(stats.getOwnerZone());
                children.add(new Path(directory, PathNormalizer.name(normalized),
                        file.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file),
                        attributes));
                listener.chunk(directory, children);
            }
            return children;
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
