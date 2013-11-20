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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.IOResumeException;
import ch.cyberduck.core.shared.DefaultAttributesFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

import ch.ethz.ssh2.SFTPOutputStream;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3FileHandle;

/**
 * @version $Id$
 */
public class SFTPWriteFeature implements Write {
    private static final Logger log = Logger.getLogger(SFTPWriteFeature.class);

    private SFTPSession session;

    public SFTPWriteFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            SFTPv3FileHandle handle;
            if(status.isAppend()) {
                handle = session.sftp().openFile(file.getAbsolute(),
                        SFTPv3Client.SSH_FXF_WRITE | SFTPv3Client.SSH_FXF_APPEND, null);
            }
            else {
                if(status.isExists() && !status.isRename()) {
                    if(file.attributes().isSymbolicLink()) {
                        // Workaround for #7327
                        session.sftp().rm(file.getAbsolute());
                    }
                }
                handle = session.sftp().openFile(file.getAbsolute(),
                        SFTPv3Client.SSH_FXF_CREAT | SFTPv3Client.SSH_FXF_TRUNC | SFTPv3Client.SSH_FXF_WRITE, null);
            }
            final OutputStream out = new SFTPOutputStream(handle);
            if(status.isAppend()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Skipping %d bytes", status.getCurrent()));
                }
                long skipped = ((SFTPOutputStream) out).skip(status.getCurrent());
                if(skipped < status.getCurrent()) {
                    throw new IOResumeException(String.format("Skipped %d bytes instead of %d", skipped, status.getCurrent()));
                }
            }
            // No parallel requests if the file size is smaller than the buffer.
            session.sftp().setRequestParallelism(
                    (int) (status.getLength() / Preferences.instance().getInteger("connection.chunksize")) + 1
            );
            return out;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Upload failed", e, file);
        }
    }

    @Override
    public Append append(final Path file, final Long length, final Cache cache) throws BackgroundException {
        if(new SFTPFindFeature(session).withCache(cache).find(file)) {
            return new Append(new DefaultAttributesFeature(session).withCache(cache).find(file).getSize());
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return true;
    }
}
