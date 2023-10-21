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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultTimestampFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class SMBTimestampFeature extends DefaultTimestampFeature {

    private final SMBSession session;

    public SMBTimestampFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        try (final DiskShare share = session.openShare(file)) {
            if(file.isDirectory()) {
                try (final Directory entry = share.openDirectory(new SMBPathContainerService(session).getKey(file),
                        Collections.singleton(AccessMask.FILE_WRITE_ATTRIBUTES),
                        Collections.singleton(FileAttributes.FILE_ATTRIBUTE_DIRECTORY),
                        Collections.singleton(SMB2ShareAccess.FILE_SHARE_READ),
                        SMB2CreateDisposition.FILE_OPEN,
                        Collections.singleton(SMB2CreateOptions.FILE_DIRECTORY_FILE))) {
                    final FileBasicInformation updatedBasicInformation = new FileBasicInformation(
                            status.getCreated() != null ? FileTime.ofEpochMillis(status.getCreated()) : FileBasicInformation.DONT_SET,
                            FileBasicInformation.DONT_SET,
                            status.getModified() != null ? FileTime.ofEpochMillis(status.getModified()) : FileBasicInformation.DONT_SET,
                            FileBasicInformation.DONT_SET,
                            FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue());
                    entry.setFileInformation(updatedBasicInformation);
                }
            }
            else {
                try (final File entry = share.openFile(new SMBPathContainerService(session).getKey(file),
                        Collections.singleton(AccessMask.FILE_WRITE_ATTRIBUTES),
                        Collections.singleton(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                        Collections.singleton(SMB2ShareAccess.FILE_SHARE_READ),
                        SMB2CreateDisposition.FILE_OVERWRITE,
                        Collections.singleton(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE))) {
                    final FileBasicInformation updatedBasicInformation = new FileBasicInformation(
                            status.getCreated() != null ? FileTime.ofEpochMillis(status.getCreated()) : FileBasicInformation.DONT_SET,
                            FileBasicInformation.DONT_SET,
                            status.getModified() != null ? FileTime.ofEpochMillis(status.getModified()) : FileBasicInformation.DONT_SET,
                            FileBasicInformation.DONT_SET,
                            FileAttributes.FILE_ATTRIBUTE_NORMAL.getValue());
                    entry.setFileInformation(updatedBasicInformation);
                }
            }
        }
        catch(IOException e) {
            throw new SMBTransportExceptionMappingService().map("Cannot read container configuration", e);
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Cannot change timestamp of {0}", e, file);
        }
        finally {
            session.releaseShare(file);
        }
    }
}
