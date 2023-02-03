package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;

public class FileidDriveListService extends AbstractDriveListService {

    private final DriveFileIdProvider provider;
    private final Path file;

    public FileidDriveListService(final DriveSession session, final DriveFileIdProvider provider, final Path file) {
        super(session, provider, 1);
        this.provider = provider;
        this.file = file;
    }

    @Override
    protected String query(final Path directory, final ListProgressListener listener) throws BackgroundException {
        // Surround with single quotes '. Escape single quotes in queries with \', e.g., 'Valentine\'s Day'.
        String escaped = file.getName();
        escaped = StringUtils.replace(escaped, "\\", "\\\\");
        escaped = StringUtils.replace(escaped, "'", "\\'");
        if(new SimplePathPredicate(DriveHomeFinderService.SHARED_FOLDER_NAME).test(directory)) {
            return String.format("name = '%s' and sharedWithMe", escaped);
        }
        return String.format("name = '%s' and '%s' in parents", escaped, provider.getFileId(directory));
    }
}
