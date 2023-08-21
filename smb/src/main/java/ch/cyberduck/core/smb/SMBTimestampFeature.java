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

import java.util.HashSet;
import java.util.Set;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.share.File;

public class SMBTimestampFeature extends DefaultTimestampFeature {

    private final SMBSession session;

    public SMBTimestampFeature(SMBSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(Path file, TransferStatus status) throws BackgroundException {
        Set<SMB2ShareAccess> shareAccessSet = new HashSet<>();
        shareAccessSet.add(SMB2ShareAccess.FILE_SHARE_READ);
        shareAccessSet.add(SMB2ShareAccess.FILE_SHARE_WRITE);
        shareAccessSet.add(SMB2ShareAccess.FILE_SHARE_DELETE);

        Set<FileAttributes> fileAttributes = new HashSet<>();
        fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
        Set<SMB2CreateOptions> createOptions = new HashSet<>();
        SMB2CreateDisposition smb2CreateDisposition = SMB2CreateDisposition.FILE_OPEN_IF;

        Set<AccessMask> accessMask = new HashSet<>();
        accessMask.add(AccessMask.MAXIMUM_ALLOWED);

        createOptions.add(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE);

        try (File fileEntry = session.share.openFile(file.getAbsolute(), accessMask, fileAttributes, shareAccessSet, smb2CreateDisposition, createOptions)) {
            FileTime creationTime = fileEntry.getFileInformation().getBasicInformation().getCreationTime();
            FileTime time = FileTime.ofEpochMillis(status.getTimestamp());

            FileBasicInformation fileBasicInformation = new FileBasicInformation(creationTime, time, time, time, FileAttributes.FILE_ATTRIBUTE_NORMAL.getValue());
            fileEntry.setFileInformation(fileBasicInformation);
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Cannot change timestamp of {0}", e, file);
        }
    }

}
