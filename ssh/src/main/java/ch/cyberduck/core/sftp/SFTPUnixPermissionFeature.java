package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.shared.DefaultUnixPermissionFeature;

import java.io.IOException;

import net.schmizz.sshj.sftp.FileAttributes;

public class SFTPUnixPermissionFeature extends DefaultUnixPermissionFeature implements UnixPermission {

    private SFTPSession session;

    public SFTPUnixPermissionFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
        final FileAttributes attr = new FileAttributes.Builder()
                .withUIDGID(new Integer(owner), 0)
                .build();
        try {
            session.sftp().setAttributes(file.getAbsolute(), attr);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    @Override
    public void setUnixGroup(final Path file, final String group) throws BackgroundException {
        final FileAttributes attr = new FileAttributes.Builder()
                .withUIDGID(0, new Integer(group))
                .build();
        try {
            session.sftp().setAttributes(file.getAbsolute(), attr);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    @Override
    public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
        final FileAttributes attr = new FileAttributes.Builder()
                .withPermissions(Integer.parseInt(permission.getMode(), 8))
                .build();
        try {
            session.sftp().setAttributes(file.getAbsolute(), attr);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
