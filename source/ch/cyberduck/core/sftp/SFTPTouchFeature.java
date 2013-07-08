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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.SFTPExceptionMappingService;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.i18n.Locale;

import java.io.IOException;
import java.text.MessageFormat;

import ch.ethz.ssh2.SFTPv3FileAttributes;

/**
 * @version $Id$
 */
public class SFTPTouchFeature implements Touch {

    private SFTPSession session;

    public SFTPTouchFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        if(file.attributes().isFile()) {
            session.message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                    file.getName()));
            try {
                final SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
                final Permission permission = new Permission(Preferences.instance().getInteger("queue.upload.permissions.file.default"));
                attr.permissions = Integer.parseInt(permission.getOctalString(), 8);
                session.sftp().createFile(file.getAbsolute(), attr);

                // Even if specified above when creating the file handle, we still need to update the
                // permissions after the creating the file. SSH_FXP_OPEN does not support setting
                // attributes in version 4 or lower.
                new SFTPUnixPermissionFeature(session).setUnixPermission(file, permission);
            }
            catch(IOException e) {
                throw new SFTPExceptionMappingService().map("Cannot create file {0}", e, file);
            }
        }
    }
}
