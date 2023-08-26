package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.ProxyOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class SMBWriteFeature extends AppendWriteFeature<Void> {
    private final SMBSession session;

    public SMBWriteFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DiskShare share = session.openShare(file);
        try {
            final File entry = share.openFile(new SMBPathContainerService(session).getKey(file),
                    Collections.singleton(AccessMask.MAXIMUM_ALLOWED),
                    Collections.singleton(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                    Collections.singleton(SMB2ShareAccess.FILE_SHARE_READ),
                    SMB2CreateDisposition.FILE_OPEN_IF,
                    Collections.singleton(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE));
            return new VoidStatusOutputStream(new SMBOutputStream(file, entry.getOutputStream(), share, entry));
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    private final class SMBOutputStream extends ProxyOutputStream {
        private final Path file;
        private final DiskShare share;
        private final File handle;

        public SMBOutputStream(final Path file, final OutputStream stream, final DiskShare share, final File handle) {
            super(stream);
            this.file = file;
            this.share = share;
            this.handle = handle;
        }

        @Override
        public void close() throws IOException {
            try {
                try {
                    try {
                        super.close();
                    }
                    finally {
                        handle.flush();
                        handle.close();
                    }
                }
                finally {
                    share.close();
                }
            }
            catch(SMBRuntimeException e) {
                throw new IOException(e);
            }
            finally {
                session.releaseShare(file);
            }
        }
    }
}
