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

import java.util.EnumSet;

import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

public class SMBListService implements ListService {

    private final SMBSession session;
    
    public SMBListService(SMBSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> result = new AttributedList<>();
        for(FileIdBothDirectoryInformation f : session.share.list(directory.getAbsolute())) {
            // TODO: add missing types and path attributes
            if((f.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0) {
                result.add(new Path(directory, f.getFileName(), EnumSet.of(AbstractPath.Type.directory)));
            }
            else {
                result.add(new Path(directory, f.getFileName(), EnumSet.of(AbstractPath.Type.file)));
            }
        }
        return result;
    }
    
}
