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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.PromptUrlProvider;

import java.io.IOException;

import com.google.api.services.drive.model.Permission;

public class DriveSharingUrlProvider implements PromptUrlProvider {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveSharingUrlProvider(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case download:
                return file.isFile();
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
            session.getClient().permissions().create(fileid.getFileid(file, new DisabledListProgressListener()),
                permission);
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        return new DriveUrlProvider().toUrl(file).find(DescriptiveUrl.Type.http);
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Object options, final PasswordCallback callback) throws BackgroundException {
        throw new UnsupportedException();
    }
}
