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

import ch.cyberduck.core.AbstractPath.Type;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

import java.util.EnumSet;

import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.common.SMBRuntimeException;

public class SMBListService implements ListService {

    private final SMBSession session;

    public SMBListService(SMBSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {

        final AttributedList<Path> result = new AttributedList<>();

        try {


            for(FileIdBothDirectoryInformation f : session.share.list(directory.getAbsolute())) {
                String fileName = f.getFileName();
                if(fileName.equals(".") || fileName.equals("..")) {
                    continue; // skip the . and .. directories
                }
                EnumSet<Type> type = EnumSet.noneOf(Type.class);
                long fileAttributes = f.getFileAttributes();

                // check for all relevant file types and add them to the EnumSet
                if((fileAttributes & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0) {
                    type.add(Type.directory);
                }
                if((fileAttributes & FileAttributes.FILE_ATTRIBUTE_NORMAL.getValue()) != 0) {
                    type.add(Type.file);
                }

                final PathAttributes attributes = new PathAttributes();
                attributes.setAccessedDate(f.getLastAccessTime().toEpochMillis());
                attributes.setModificationDate(f.getLastWriteTime().toEpochMillis());
                attributes.setCreationDate(f.getCreationTime().toEpochMillis());
                attributes.setSize(f.getEndOfFile());
                attributes.setDisplayname(f.getFileName());

                // default to file
                if(type.isEmpty()) {
                    type.add(Type.file);
                }

                result.add(new Path(directory, fileName, type, attributes));

            }
            return result;
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
