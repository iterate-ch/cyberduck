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
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.preferences.HostPreferences;

import java.io.IOException;

import com.google.api.services.drive.model.Permission;

public class DriveSharingUrlProvider implements PromptUrlProvider {

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
    public DescriptiveUrl toDownloadUrl(final Path file, final Object options, final PasswordCallback callback) throws BackgroundException {
        final Permission permission = new Permission();
        // To make a file public you will need to assign the role reader to the type anyone
        permission.setRole("reader");
        permission.setType("anyone");
        try {
            session.getClient().permissions().create(fileid.getFileId(file), permission)
                .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable")).execute();
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Failure to write attributes of {0}", e, file);
        }
        return new DriveUrlProvider().toUrl(file).find(DescriptiveUrl.Type.http);
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Object options, final PasswordCallback callback) throws BackgroundException {
        return DescriptiveUrl.EMPTY;
    }
}
