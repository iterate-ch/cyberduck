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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.model.File;

public class DriveCopyFeature implements Copy {

    private final DriveSession session;

    public DriveCopyFeature(final DriveSession session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            session.getClient().files().copy(new DriveFileidProvider(session).getFileid(source, new DisabledListProgressListener()), new File()
                    .setParents(Collections.singletonList(new DriveFileidProvider(session).getFileid(target.getParent(), new DisabledListProgressListener())))
                    .setName(target.getName())).execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return true;
    }

    @Override
    public Copy withTarget(final Session<?> session) {
        return this;
    }
}
