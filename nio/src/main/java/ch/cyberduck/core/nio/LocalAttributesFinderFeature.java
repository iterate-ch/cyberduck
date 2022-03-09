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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

public class LocalAttributesFinderFeature implements AttributesFinder, AttributesAdapter<BasicFileAttributes> {

    private final LocalSession session;

    public LocalAttributesFinderFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            final Class<? extends BasicFileAttributes> provider = session.isPosixFilesystem() ? PosixFileAttributes.class : DosFileAttributes.class;
            final java.nio.file.Path impl = session.toPath(file);
            final PathAttributes attr = this.toAttributes(Files.readAttributes(impl, provider, LinkOption.NOFOLLOW_LINKS));
            if(!session.isPosixFilesystem()) {
                Permission.Action actions = Permission.Action.none;
                if(Files.isReadable(impl)) {
                    actions = actions.or(Permission.Action.read);
                }
                if(Files.isWritable(impl)) {
                    actions = actions.or(Permission.Action.write);
                }
                if(Files.isExecutable(impl)) {
                    actions = actions.or(Permission.Action.execute);
                }
                attr.setPermission(new Permission(
                        actions, Permission.Action.none, Permission.Action.none
                ));
            }
            return attr;
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public PathAttributes toAttributes(final BasicFileAttributes f) {
        final PathAttributes attributes = new PathAttributes();
        if(f.isRegularFile()) {
            attributes.setSize(f.size());
        }
        attributes.setModificationDate(f.lastModifiedTime().toMillis());
        attributes.setCreationDate(f.creationTime().toMillis());
        attributes.setAccessedDate(f.lastAccessTime().toMillis());
        if(session.isPosixFilesystem()) {
            attributes.setOwner(((PosixFileAttributes) f).owner().getName());
            attributes.setGroup(((PosixFileAttributes) f).group().getName());
            attributes.setPermission(new Permission(PosixFilePermissions.toString(((PosixFileAttributes) f).permissions())));
        }
        return attributes;
    }
}
