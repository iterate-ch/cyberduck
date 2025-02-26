package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.drive.model.Permission;

public class DriveSharingUrlProvider implements Share {

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveSharingUrlProvider(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case download:
                return true;
        }
        return false;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, final Object options, final PasswordCallback callback) throws BackgroundException {
        final Permission permission = new Permission();
        // To make a file public you will need to assign the role reader to the type anyone
        permission.setRole("reader");
        permission.setType("anyone");
        try {
            session.getClient().permissions().create(fileid.getFileId(file), permission)
                    .setSupportsAllDrives(HostPreferencesFactory.get(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Failure to write attributes of {0}", e, file);
        }
        return new DriveUrlProvider().toUrl(file, EnumSet.of(DescriptiveUrl.Type.http)).find(DescriptiveUrl.Type.http);
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, final Object options, final PasswordCallback callback) throws BackgroundException {
        return DescriptiveUrl.EMPTY;
    }
}
