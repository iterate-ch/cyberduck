package ch.cyberduck.core.ftp;

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

import org.apache.log4j.Logger;

import java.io.IOException;

public class FTPUnixPermissionFeature extends DefaultUnixPermissionFeature implements UnixPermission {
    private static final Logger log = Logger.getLogger(FTPUnixPermissionFeature.class);

    private FTPSession session;

    private BackgroundException failure;

    public FTPUnixPermissionFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
        final String command = "chown";
        try {
            if(!session.getClient().sendSiteCommand(String.format("%s %s %s", command, owner, file.getAbsolute()))) {
                throw new FTPException(session.getClient().getReplyCode(),
                        session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change owner", e, file);
        }
    }

    @Override
    public void setUnixGroup(final Path file, final String group) throws BackgroundException {
        final String command = "chgrp";
        try {
            if(!session.getClient().sendSiteCommand(String.format("%s %s %s", command, group, file.getAbsolute()))) {
                throw new FTPException(session.getClient().getReplyCode(),
                        session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change group", e, file);
        }
    }

    @Override
    public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
        if(failure != null) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Skip setting permission for %s due to previous failure %s", file, failure.getMessage()));
            }
            throw failure;
        }
        try {
            if(!session.getClient().sendSiteCommand(String.format("CHMOD %s %s", permission.getMode(), file.getAbsolute()))) {
                throw new FTPException(session.getClient().getReplyCode(),
                        session.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw failure = new FTPExceptionMappingService().map("Cannot change permissions of {0}", e, file);
        }
    }
}