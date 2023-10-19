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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete.Callback;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;

public class SMBMoveFeature implements Move {

    private final SMBSession session;

    public SMBMoveFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public Path move(final Path source, final Path target, final TransferStatus status, final Callback delete, final ConnectionCallback prompt) throws BackgroundException {
        try (final DiskShare share = session.openShare(source)) {
            try (DiskEntry file = share.open(new SMBPathContainerService(session).getKey(source),
                    Collections.singleton(AccessMask.DELETE),
                    Collections.singleton(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                    Collections.singleton(SMB2ShareAccess.FILE_SHARE_READ),
                    SMB2CreateDisposition.FILE_OPEN,
                    Collections.singleton(source.isDirectory() ? SMB2CreateOptions.FILE_DIRECTORY_FILE : SMB2CreateOptions.FILE_NON_DIRECTORY_FILE))) {
                file.rename(new SmbPath(share.getSmbPath(), new SMBPathContainerService(session).getKey(target)).getPath(), status.isExists());
            }
        }
        catch(IOException e) {
            throw new SMBTransportExceptionMappingService().map("Cannot read container configuration", e);
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Cannot rename {0}", e, source);
        }
        finally {
            session.releaseShare(source);
        }
        return target;
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        final SMBPathContainerService containerService = new SMBPathContainerService(session);
        if(!containerService.getContainer(source).equals(containerService.getContainer(target))) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source)).withFile(source);
        }
    }
}
