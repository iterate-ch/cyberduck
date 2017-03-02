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
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveJsonRequest;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;

public class OneDriveDirectoryFeature implements Directory {
    private static final Logger log = Logger.getLogger(OneDriveDirectoryFeature.class);

    private final OneDriveSession session;

    public OneDriveDirectoryFeature(OneDriveSession session) {
        this.session = session;
    }

    @Override
    public void mkdir(final Path directory) throws BackgroundException {
        this.mkdir(directory, null, new TransferStatus());
    }

    @Override
    public void mkdir(final Path directory, final String region, final TransferStatus status) throws BackgroundException {
        if(directory.isRoot() || directory.getParent().isRoot()) {
            throw new BackgroundException("Cannot create directory here", "Create directory in container");
        }

        // evaluating query
        final OneDriveUrlBuilder builder = new OneDriveUrlBuilder(session)
                .resolveDriveQueryPath(directory.getParent())
                .resolveChildrenPath(directory.getParent());

        try {
            final OneDriveJsonRequest request = new OneDriveJsonRequest(builder.build(), "POST");
            JsonObject requestObject = new JsonObject();
            requestObject.add("name", directory.getName());
            requestObject.add("folder", new JsonObject());
            request.sendRequest(session.getClient().getExecutor()).close();
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public Directory withWriter(final Write writer) {
        return this;
    }
}
