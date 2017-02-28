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
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultUnixPermissionFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;

public class LocalUnixPermissionFeature extends DefaultUnixPermissionFeature {

    private final LocalSession session;

    public LocalUnixPermissionFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
        try {
            final UserPrincipal principal = session.getClient().getUserPrincipalLookupService().lookupPrincipalByName(owner);
            Files.setOwner(session.getClient().getPath(file.getAbsolute()), principal);
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    @Override
    public void setUnixGroup(final Path file, final String group) throws BackgroundException {
        try {
            final GroupPrincipal principal = session.getClient().getUserPrincipalLookupService().lookupPrincipalByGroupName(group);
            Files.getFileAttributeView(session.getClient().getPath(file.getAbsolute()),
                    PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(principal);
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    @Override
    public Permission getUnixPermission(final Path file) throws BackgroundException {
        return new LocalAttributesFinderFeature(session).find(file).getPermission();
    }

    @Override
    public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
        try {
            Files.setPosixFilePermissions(session.getClient().getPath(file.getAbsolute()), PosixFilePermissions.fromString(permission.getSymbol()));
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
