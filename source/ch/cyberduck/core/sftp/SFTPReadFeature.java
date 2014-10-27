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
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;

/**
 * @version $Id$
 */
public class SFTPReadFeature implements Read {
    private static final Logger log = Logger.getLogger(SFTPReadFeature.class);

    private SFTPSession session;

    public SFTPReadFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        InputStream in;
        try {
            final RemoteFile handle = session.sftp().open(file.getAbsolute(),
                    EnumSet.of(OpenMode.READ));
            final int maxUnconfirmedReads
                    = (int) (status.getLength() / Preferences.instance().getInteger("connection.chunksize")) + 1;
            if(status.isAppend()) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Skipping %d bytes", status.getCurrent()));
                }
                in = handle.new ReadAheadRemoteFileInputStream(maxUnconfirmedReads, status.getCurrent()) {
                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        }
                        finally {
                            handle.close();
                        }
                    }
                };
            }
            else {
                in = handle.new ReadAheadRemoteFileInputStream(maxUnconfirmedReads, 0L) {
                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        }
                        finally {
                            handle.close();
                        }
                    }
                };
            }
            return in;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean append(final Path file) {
        return true;
    }
}
