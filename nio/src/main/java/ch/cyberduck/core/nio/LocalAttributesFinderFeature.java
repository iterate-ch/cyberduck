package ch.cyberduck.core.nio;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

public class LocalAttributesFinderFeature implements AttributesFinder {

    private final LocalSession session;

    public LocalAttributesFinderFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        try {
            return this.convert(session.getClient().getPath(file.getAbsolute()));
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public AttributesFinder withCache(final PathCache cache) {
        throw new UnsupportedOperationException();
    }

    public PathAttributes convert(final java.nio.file.Path path) throws IOException {
        final boolean isPosix = path.getFileSystem().supportedFileAttributeViews().contains("posix");
        final PathAttributes attributes = new PathAttributes();
        final Class provider = isPosix ? PosixFileAttributes.class : DosFileAttributes.class;
        final BasicFileAttributes a = Files.readAttributes(path, provider);
        attributes.setSize(a.size());
        attributes.setModificationDate(a.lastModifiedTime().toMillis());
        attributes.setCreationDate(a.creationTime().toMillis());
        attributes.setAccessedDate(a.lastAccessTime().toMillis());
        if(isPosix) {
            attributes.setOwner(((PosixFileAttributes) a).owner().getName());
            attributes.setGroup(((PosixFileAttributes) a).group().getName());
            attributes.setPermission(new Permission(PosixFilePermissions.toString(((PosixFileAttributes) a).permissions())));
        }
        return attributes;
    }
}
