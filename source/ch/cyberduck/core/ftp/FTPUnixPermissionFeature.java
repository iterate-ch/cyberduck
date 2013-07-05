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
import ch.cyberduck.core.exception.FTPExceptionMappingService;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.i18n.Locale;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * @version $Id:$
 */
public class FTPUnixPermissionFeature implements UnixPermission {

    private FTPSession session;

    public FTPUnixPermissionFeature(final FTPSession session) {
        this.session = session;
    }

    @Override
    public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
        session.message(MessageFormat.format(Locale.localizedString("Changing owner of {0} to {1}", "Status"),
                file.getName(), owner));

        String command = "chown";
        try {
            if(file.attributes().isFile() && !file.attributes().isSymbolicLink()) {
                if(!session.getClient().sendSiteCommand(String.format("%s %s %s", command, owner, file.getAbsolute()))) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
            else if(file.attributes().isDirectory()) {
                if(!session.getClient().sendSiteCommand(String.format("%s %s %s", command, owner, file.getAbsolute()))) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change owner", e, file);
        }
    }


    @Override
    public void setUnixGroup(final Path file, final String group) throws BackgroundException {
        session.message(MessageFormat.format(Locale.localizedString("Changing group of {0} to {1}", "Status"),
                file.getName(), group));

        String command = "chgrp";
        try {
            if(file.attributes().isFile() && !file.attributes().isSymbolicLink()) {
                if(!session.getClient().sendSiteCommand(String.format("%s %s %s", command, group, file.getAbsolute()))) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
            else if(file.attributes().isDirectory()) {
                if(!session.getClient().sendSiteCommand(String.format("%s %s %s", command, group, file.getAbsolute()))) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change group", e, file);
        }
    }

    @Override
    public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
        session.message(MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                file.getName(), permission.getOctalString()));
        try {
            if(file.attributes().isFile() && !file.attributes().isSymbolicLink()) {
                if(!session.getClient().sendSiteCommand(String.format("CHMOD %s %s", permission.getOctalString(), file.getAbsolute()))) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
            else if(file.attributes().isDirectory()) {
                if(!session.getClient().sendSiteCommand(String.format("CHMOD %s %s", permission.getOctalString(), file.getAbsolute()))) {
                    throw new FTPException(session.getClient().getReplyCode(),
                            session.getClient().getReplyString());
                }
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map("Cannot change permissions", e, file);
        }
    }
}