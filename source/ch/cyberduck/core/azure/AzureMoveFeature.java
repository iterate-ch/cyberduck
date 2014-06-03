package ch.cyberduck.core.azure;

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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Move;

import java.util.Collections;

/**
 * @version $Id$
 */
public class AzureMoveFeature implements Move {

    private AzureSession session;

    private PathContainerService containerService
            = new AzurePathContainerService();

    public AzureMoveFeature(AzureSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(Path file) {
        return !containerService.isContainer(file);
    }

    @Override
    public void move(Path file, Path renamed, boolean exists) throws BackgroundException {
        if(file.isFile()) {
            new AzureCopyFeature(session).copy(file, renamed);
            new AzureDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginController());
        }
        else if(file.isDirectory()) {
            for(Path i : session.list(file, new DisabledListProgressListener())) {
                this.move(i, new Path(renamed, i.getName(), i.getType()), false);
            }
        }
    }
}
