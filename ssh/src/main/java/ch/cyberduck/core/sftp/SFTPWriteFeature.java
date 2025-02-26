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
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.ChunkedOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;

public class SFTPWriteFeature implements Write<Void> {
    private static final Logger log = LogManager.getLogger(SFTPWriteFeature.class);

    private final SFTPSession session;
    private final PreferencesReader preferences;

    public SFTPWriteFeature(final SFTPSession session) {
        this.session = session;
        this.preferences = HostPreferencesFactory.get(session.getHost());
    }

    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final EnumSet<OpenMode> flags;
            if(status.isAppend()) {
                if(status.isExists()) {
                    // No append flag. Otherwise the offset field of SSH_FXP_WRITE requests is ignored.
                    flags = EnumSet.of(OpenMode.WRITE);
                }
                else {
                    // Allocate offset
                    flags = EnumSet.of(OpenMode.CREAT, OpenMode.WRITE);
                }
            }
            else {
                // A new file is created; if the file already exists, it is opened and truncated to preserve ownership of file.
                if(status.isExists()) {
                    if(file.isSymbolicLink()) {
                        // Workaround for #7327
                        session.sftp().remove(file.getAbsolute());
                        flags = EnumSet.of(OpenMode.CREAT, OpenMode.TRUNC, OpenMode.WRITE);
                    }
                    else {
                        flags = EnumSet.of(OpenMode.TRUNC, OpenMode.WRITE);
                    }
                }
                else {
                    flags = EnumSet.of(OpenMode.CREAT, OpenMode.TRUNC, OpenMode.WRITE);
                }
            }
            final RemoteFile handle = session.sftp().open(file.getAbsolute(), flags);
            final int maxUnconfirmedWrites = this.getMaxUnconfirmedWrites(status);
            log.info("Using {} unconfirmed writes", maxUnconfirmedWrites);
            log.info("Skipping {} bytes", status.getOffset());
            // Open stream at offset
            return new VoidStatusOutputStream(new ChunkedOutputStream(handle.new RemoteFileOutputStream(status.getOffset(), maxUnconfirmedWrites) {
                private final AtomicBoolean close = new AtomicBoolean();

                @Override
                public void close() throws IOException {
                    if(close.get()) {
                        log.warn("Skip double close of stream {}", this);
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
            }, preferences.getInteger("sftp.write.chunksize")));
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    protected int getMaxUnconfirmedWrites(final TransferStatus status) {
        if(TransferStatus.UNKNOWN_LENGTH == status.getLength()) {
            return preferences.getInteger("sftp.write.maxunconfirmed");
        }
        return Integer.min((int) (status.getLength() / preferences.getInteger("connection.chunksize")) + 1,
                preferences.getInteger("sftp.write.maxunconfirmed"));
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.random);
    }
}
