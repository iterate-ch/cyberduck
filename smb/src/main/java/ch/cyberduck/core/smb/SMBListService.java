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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.common.SMBRuntimeException;

public class SMBListService implements ListService {
    private static final Logger log = LogManager.getLogger(SMBListService.class);

    private final SMBSession session;

    public SMBListService(final SMBSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> result = new AttributedList<>();
        try {
            for(FileIdBothDirectoryInformation f : session.share.list(directory.getAbsolute())) {
                final String filename = f.getFileName();
                if(filename.equals(".") || filename.equals("..")) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Skip %s", f.getFileName()));
                    }
                    continue;
                }
                final EnumSet<Type> type = EnumSet.noneOf(Type.class);
                long fileAttributes = f.getFileAttributes();
                // check for all relevant file types and add them to the EnumSet
                if((fileAttributes & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0) {
                    type.add(Type.directory);
                }
                else {
                    type.add(Type.file);
                }
                final PathAttributes attr = new PathAttributes();
                attr.setAccessedDate(f.getLastAccessTime().toEpochMillis());
                attr.setModificationDate(f.getLastWriteTime().toEpochMillis());
                attr.setCreationDate(f.getCreationTime().toEpochMillis());
                attr.setSize(f.getEndOfFile());
                attr.setDisplayname(f.getFileName());
                result.add(new Path(directory, filename, type, attr));
            }
            return result;
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
