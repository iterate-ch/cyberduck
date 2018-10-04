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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.model.File;

public class DriveCopyFeature implements Copy {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveCopyFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final File copy = session.getClient().files().copy(fileid.getFileid(source, new DisabledListProgressListener()), new File()
                .setParents(Collections.singletonList(fileid.getFileid(target.getParent(), new DisabledListProgressListener())))
                .setName(target.getName()))
                .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable")).execute();
            return new Path(target.getParent(), target.getName(), target.getType(),
                    new PathAttributes(target.attributes()).withVersionId(copy.getId()));
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
        if(target.isRoot()) {
            return false;
        }
        return !source.getType().contains(Path.Type.placeholder);
    }
}
