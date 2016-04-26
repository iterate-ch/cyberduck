package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.model.File;

public class DriveCopyFeature implements Copy {

    private DriveSession session;

    public DriveCopyFeature(DriveSession session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path copy) throws BackgroundException {
        try {
            session.getClient().files().copy(new DriveFileidProvider(session).getFileid(source), new File()
                    .setParents(Collections.singletonList(new DriveFileidProvider(session).getFileid(copy.getParent())))
                    .setName(copy.getName())).execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }
}
