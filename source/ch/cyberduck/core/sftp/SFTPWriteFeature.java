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
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;

/**
 * @version $Id$
 */
public class SFTPWriteFeature extends AppendWriteFeature {
    private static final Logger log = Logger.getLogger(SFTPWriteFeature.class);

    private SFTPSession session;

    private Preferences preferences
            = PreferencesFactory.get();

    public SFTPWriteFeature(final SFTPSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final RemoteFile handle;
            if(status.isAppend()) {
                handle = session.sftp().open(file.getAbsolute(),
                        EnumSet.of(OpenMode.WRITE, OpenMode.APPEND));
            }
            else {
                if(status.isExists() && !status.isRename()) {
                    if(file.isSymbolicLink()) {
                        // Workaround for #7327
                        session.sftp().remove(file.getAbsolute());
                    }
                }
                handle = session.sftp().open(file.getAbsolute(),
                        EnumSet.of(OpenMode.CREAT, OpenMode.TRUNC, OpenMode.WRITE));
            }
            final int maxUnconfirmedWrites
                    = (int) (status.getLength() / preferences.getInteger("connection.chunksize")) + 1;
            if(log.isInfoEnabled()) {
                log.info(String.format("Skipping %d bytes", status.getCurrent()));
            }
            return handle.new RemoteFileOutputStream(status.getCurrent(), maxUnconfirmedWrites) {
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
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    @Override
    public boolean temporary() {
        return true;
    }
}
