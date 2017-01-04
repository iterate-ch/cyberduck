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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import java.util.Collections;

public class DriveMoveFeature implements Move {

    private final DriveSession session;

    public DriveMoveFeature(DriveSession session) {
        this.session = session;
    }

    @Override
    public boolean isSupported(Path source, final Path target) {
        return true;
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        final Delete delete = session.getFeature(Delete.class);
        if(file.isDirectory()) {
            new DriveDirectoryFeature(session).mkdir(renamed);
            for(Path i : session.getFeature(ListService.class).list(file, new DisabledListProgressListener())) {
                this.move(i, new Path(renamed, i.getName(), i.getType()), false, callback);
            }
            delete.delete(Collections.singletonList(file),
                    new DisabledLoginCallback(), callback);
        }
        else {
            new DriveCopyFeature(session).copy(file, renamed);
            delete.delete(Collections.singletonList(file),
                    new DisabledLoginCallback(), callback);
        }
    }
}
