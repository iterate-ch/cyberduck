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
        OneDriveDrive drive = new OneDriveDrive(session.getClient(), new OneDriveHomeFinderFeature(session).find().getName());
        final OneDriveDrive.Metadata metadata;
        try {
            metadata = drive.getMetadata();
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Cannot get space {0}", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot get space {0}", e);
        }
        return new Space(metadata.getUsed(), metadata.getTotal());
    }
}
