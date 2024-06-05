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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import net.schmizz.sshj.sftp.FileAttributes;

public class SFTPTimestampFeature implements Timestamp {

    private final SFTPSession session;

    public SFTPTimestampFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(null != status.getModified()) {
                // We must both set the accessed and modified time. See AttribFlags.SSH_FILEXFER_ATTR_V3_ACMODTIME
                // All times are represented as seconds from Jan 1, 1970 in UTC.
                final long atime = Timestamp.toSeconds(System.currentTimeMillis());
                final long mtime = Timestamp.toSeconds(status.getModified() != null ? status.getModified() : System.currentTimeMillis());
                final FileAttributes attrs = new FileAttributes.Builder().withAtimeMtime(atime, mtime).build();
                session.sftp().setAttributes(file.getAbsolute(), attrs);
                status.setResponse(new PathAttributes(status.getResponse()).withModificationDate(mtime));
            }
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot change timestamp of {0}", e, file);
        }
    }
}
