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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;

import java.io.IOException;

import ch.ethz.ssh2.SFTPv3FileAttributes;

/**
 * @version $Id$
 */
public class SFTPTimestampFeature implements Timestamp {

    private SFTPSession session;

    public SFTPTimestampFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final Long created, final Long modified, final Long accessed) throws BackgroundException {
        try {
            final SFTPv3FileAttributes attrs = new SFTPv3FileAttributes();
            // We must both set the accessed and modified time. See AttribFlags.SSH_FILEXFER_ATTR_V3_ACMODTIME
            attrs.atime = (int) (System.currentTimeMillis() / 1000);
            attrs.mtime = (int) (modified / 1000);
            session.sftp().setstat(file.getAbsolute(), attrs);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot change timestamp", e, file);
        }

    }
}
