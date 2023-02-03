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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;

public class SFTPReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(SFTPReadFeature.class);

    private final SFTPSession session;

    public SFTPReadFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final RemoteFile handle = session.sftp().open(file.getAbsolute(), EnumSet.of(OpenMode.READ));
            final int maxUnconfirmedReads = this.getMaxUnconfirmedReads(status);
            if(log.isInfoEnabled()) {
                log.info(String.format("Skipping %d bytes", status.getOffset()));
            }
            return handle.new ReadAheadRemoteFileInputStream(maxUnconfirmedReads, status.getOffset(), status.getLength()) {
                private final AtomicBoolean close = new AtomicBoolean();

                @Override
                public void close() throws IOException {
                    if(close.get()) {
                        log.warn(String.format("Skip double close of stream %s", this));
                        return;
                    }
                    try {
                        super.close();
                    }
                    finally {
                        handle.close();
                        close.set(true);
                    }
                }
            };
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    protected int getMaxUnconfirmedReads(final TransferStatus status) {
        final PreferencesReader preferences = new HostPreferences(session.getHost());
        if(TransferStatus.UNKNOWN_LENGTH == status.getLength()) {
            return preferences.getInteger("sftp.read.maxunconfirmed");
        }
        return Integer.min(((int) (status.getLength() / preferences.getInteger("connection.chunksize")) + 1),
            preferences.getInteger("sftp.read.maxunconfirmed"));
    }
}
