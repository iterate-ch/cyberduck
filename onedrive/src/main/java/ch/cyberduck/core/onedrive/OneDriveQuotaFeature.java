package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveDrive;

import java.io.IOException;

public class OneDriveQuotaFeature implements Quota {

    private final OneDriveSession session;

    public OneDriveQuotaFeature(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public Space get() throws BackgroundException {
        final Path home = new OneDriveHomeFinderFeature(session).find();
        final OneDriveDrive.Metadata metadata;
        try {
            metadata = new OneDriveDrive(session.getClient(), home.getName()).getMetadata();
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Failure to read attributes of {0}", e, home);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, home);
        }
        return new Space(
            metadata.getUsed() != null ? metadata.getUsed() : 0,
            metadata.getTotal() != null ? metadata.getTotal(), Long.MAX_VALUE);
    }
}
