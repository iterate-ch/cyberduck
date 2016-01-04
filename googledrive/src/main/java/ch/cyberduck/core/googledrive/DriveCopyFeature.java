package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.model.File;

/**
 * @version $Id:$
 */
public class DriveCopyFeature implements Copy {

    private DriveSession session;

    public DriveCopyFeature(DriveSession session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path copy) throws BackgroundException {
        try {
            session.getClient().files().copy(source.attributes().getVersionId(), new File()
                    .setParents(Collections.singletonList(copy.getParent().attributes().getVersionId()))
                    .setName(copy.getName()));
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }
}
