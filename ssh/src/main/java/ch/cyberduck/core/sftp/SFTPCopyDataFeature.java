package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.PacketType;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.Request;
import net.schmizz.sshj.sftp.SFTPEngine;

/**
 * Server side copy using the <code>copy-data</code> extension as implemented by OpenSSH sftp-server
 * (draft-ietf-secsh-filexfer-extensions, <code>PROTOCOL</code> in OpenSSH). Operates on open file handles.
 */
public class SFTPCopyDataFeature implements Copy {

    public static final String EXTENSION = "copy-data";

    private final SFTPSession session;

    public SFTPCopyDataFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback prompt, final StreamListener listener) throws BackgroundException {
        try {
            try (final HandleFile read = this.open(source, EnumSet.of(OpenMode.READ))) {
                final Set<OpenMode> modes = EnumSet.of(OpenMode.WRITE, OpenMode.CREAT);
                if(status.isExists()) {
                    modes.add(OpenMode.TRUNC);
                }
                else {
                    modes.add(OpenMode.EXCL);
                }
                try (final HandleFile write = this.open(target, modes)) {
                    final Request request = session.sftp().newExtendedRequest(EXTENSION)
                            .putString(read.handle())
                            .putUInt64(0L)
                            // Zero length copies until end of file
                            .putUInt64(0L)
                            .putString(write.handle())
                            .putUInt64(0L);
                    session.sftp().request(request).retrieve().ensureStatusPacketIsOK();
                }
            }
            listener.sent(status.getLength());
            return new Path(target).withAttributes(new SFTPAttributesFinderFeature(session).find(target));
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    private HandleFile open(final Path file, final Set<OpenMode> modes) throws IOException, BackgroundException {
        final byte[] handle = session.sftp().request(session.sftp().newRequest(PacketType.OPEN)
                        .putString(file.getAbsolute(), session.sftp().getSubsystem().getRemoteCharset())
                        .putUInt32(OpenMode.toMask(modes))
                        .putFileAttributes(FileAttributes.EMPTY))
                .retrieve().ensurePacketTypeIs(PacketType.HANDLE).readBytes();
        return new HandleFile(session.sftp(), file.getAbsolute(), handle);
    }

    /**
     * Expose opaque handle for use in extended request
     */
    private static final class HandleFile extends RemoteFile {
        HandleFile(final SFTPEngine engine, final String path, final byte[] handle) {
            super(engine, path, handle);
        }

        byte[] handle() {
            return handle;
        }
    }
}
