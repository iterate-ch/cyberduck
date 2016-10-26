package ch.cyberduck.core.irods;

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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;

public class IRODSAttributesFeature implements Attributes {

    private final IRODSSession session;

    public IRODSAttributesFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.filesystem();
            final IRODSFile f = fs.getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
            if(!f.exists()) {
                throw new NotfoundException(file.getAbsolute());
            }
            final PathAttributes attributes = new PathAttributes();
            final ObjStat stats = fs.getObjStat(f.getAbsolutePath());
            attributes.setModificationDate(stats.getModifiedAt().getTime());
            attributes.setCreationDate(stats.getCreatedAt().getTime());
            attributes.setSize(stats.getObjSize());
            attributes.setChecksum(Checksum.parse(Hex.encodeHexString(Base64.decodeBase64(stats.getChecksum()))));
            attributes.setOwner(stats.getOwnerName());
            attributes.setGroup(stats.getOwnerZone());
            return attributes;
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public Attributes withCache(final PathCache cache) {
        return this;
    }
}
