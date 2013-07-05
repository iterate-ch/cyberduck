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
import ch.cyberduck.core.exception.SFTPExceptionMappingService;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.i18n.Locale;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;

import ch.ethz.ssh2.SFTPv3FileAttributes;

/**
 * @version $Id:$
 */
public class SFTPUnixPermissionFeature implements UnixPermission {
    private static final Logger log = Logger.getLogger(SFTPUnixPermissionFeature.class);

    private SFTPSession session;

    public SFTPUnixPermissionFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void setUnixOwner(final Path file, final String owner) throws BackgroundException {
        session.message(MessageFormat.format(Locale.localizedString("Changing owner of {0} to {1}", "Status"),
                file.getName(), owner));

        SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
        attr.uid = new Integer(owner);
        try {
            session.sftp().setstat(file.getAbsolute(), attr);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot write file attributes", e, file);
        }
    }

    @Override
    public void setUnixGroup(final Path file, final String group) throws BackgroundException {
        session.message(MessageFormat.format(Locale.localizedString("Changing group of {0} to {1}", "Status"),
                file.getName(), group));

        SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
        attr.gid = new Integer(group);
        try {
            session.sftp().setstat(file.getAbsolute(), attr);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot write file attributes", e, file);
        }
    }

    @Override
    public void setUnixPermission(final Path file, final Permission permission) throws BackgroundException {
        session.message(MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                file.getName(), permission.getOctalString()));

        SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
        attr.permissions = Integer.parseInt(permission.getOctalString(), 8);
        try {
            session.sftp().setstat(file.getAbsolute(), attr);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot write file attributes", e, file);
        }
    }
}
