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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

public class FileidDriveListService extends AbstractDriveListService {
    private final DriveFileidProvider provider;
    private final Path file;

    public FileidDriveListService(final DriveSession session, final DriveFileidProvider provider, final Path file) {
        super(session, 1);
        this.provider = provider;
        this.file = file;
    }

    @Override
    protected String query(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return String.format("name = '%s' and '%s' in parents", file.getName(), provider.getFileid(directory, new DisabledListProgressListener()));
    }

    @Override
    protected String getSpaces() {
        return String.format("%s,%s", DriveListService.SPACE_DRIVE, DriveListService.SPACE_PHOTOS);
    }
}
