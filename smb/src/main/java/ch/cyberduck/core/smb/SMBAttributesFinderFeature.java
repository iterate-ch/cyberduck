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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.smbj.common.SMBRuntimeException;

public class SMBAttributesFinderFeature implements AttributesFinder {

    private final SMBSession session;

    public SMBAttributesFinderFeature(final SMBSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        final PathAttributes attributes = new PathAttributes();
        try {
            final FileAllInformation fileInformation = session.share.getFileInformation(file.getAbsolute());
            if(file.isDirectory() && !fileInformation.getStandardInformation().isDirectory()) {
                throw new NotfoundException(String.format("File %s found but type is not directory", file.getName()));
            }
            else if(file.isFile() && fileInformation.getStandardInformation().isDirectory()) {
                throw new NotfoundException(String.format("File %s found but type is not file", file.getName()));
            }
            attributes.setAccessedDate(fileInformation.getBasicInformation().getLastAccessTime().toEpochMillis());
            attributes.setModificationDate(fileInformation.getBasicInformation().getLastWriteTime().toEpochMillis());
            attributes.setCreationDate(fileInformation.getBasicInformation().getCreationTime().toEpochMillis());
            attributes.setSize(fileInformation.getStandardInformation().getEndOfFile());
            return attributes;
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }
}
