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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.protocol.commons.buffer.Buffer.BufferException;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class SMBCopyFeature implements Copy {

    private final SMBSession session;

    public SMBCopyFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status,
                     final ConnectionCallback prompt, final StreamListener listener) throws BackgroundException {
        try (final DiskShare share = session.openShare(source)) {
            try (final File sourceFile = share.openFile(new SMBPathContainerService(session).getKey(source),
                    new HashSet<>(Arrays.asList(AccessMask.FILE_READ_DATA, AccessMask.FILE_READ_ATTRIBUTES)),
                    Collections.singleton(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                    Collections.singleton(SMB2ShareAccess.FILE_SHARE_READ),
                    SMB2CreateDisposition.FILE_OPEN,
                    Collections.singleton(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE));
                 final File targetFile = share.openFile(new SMBPathContainerService(session).getKey(target),
                         Collections.singleton(AccessMask.MAXIMUM_ALLOWED),
                         Collections.singleton(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                         Collections.singleton(SMB2ShareAccess.FILE_SHARE_READ),
                         status.isExists() ? SMB2CreateDisposition.FILE_OVERWRITE : SMB2CreateDisposition.FILE_CREATE,
                         Collections.singleton(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE))) {
                sourceFile.remoteCopyTo(targetFile);
            }
        }
        catch(IOException e) {
            throw new SMBTransportExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        catch(BufferException e) {
            throw new BackgroundException(e);
        }
        finally {
            session.releaseShare(source);
        }
        return target;
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        final SMBPathContainerService containerService = new SMBPathContainerService(session);
        // Remote copy is only possible between files on the same server
        if(!containerService.getContainer(source).equals(containerService.getContainer(target))) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source.getName())).withFile(source);
        }
    }
}
