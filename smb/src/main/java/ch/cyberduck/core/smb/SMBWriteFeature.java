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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.output.ProxyOutputStream;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.File;

public class SMBWriteFeature extends AppendWriteFeature<Void> {
    private final SMBSession session;

    public SMBWriteFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<Void> write(Path file, TransferStatus status, ConnectionCallback callback)
            throws BackgroundException {
        try {
            Set<AccessMask> accessMask = new HashSet<>();
            accessMask.add(AccessMask.MAXIMUM_ALLOWED);


            File fileEntry = session.share.openFile(file.getAbsolute(), accessMask,
                    Collections.singleton(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                    Collections.singleton(SMB2ShareAccess.FILE_SHARE_READ), SMB2CreateDisposition.FILE_OPEN_IF,
                    Collections.singleton(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE));

            return new VoidStatusOutputStream(new SMBOutputStream(fileEntry.getOutputStream(), fileEntry));
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Upload {0} failed", e, file);
        }


    }

    private static final class SMBOutputStream extends ProxyOutputStream {

        private final File file;
        private long fileSize;

        public SMBOutputStream(OutputStream stream, File file) {
            super(stream);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                file.flush();
                file.setLength(fileSize);
                file.close();
            }
        }

        @Override
        protected void afterWrite(int n) throws IOException {
            fileSize += n;
            super.afterWrite(n);
        }

    }

}
