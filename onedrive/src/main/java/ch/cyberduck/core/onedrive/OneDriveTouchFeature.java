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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveJsonRequest;

import java.io.IOException;
import java.net.URL;

import com.eclipsesource.json.JsonObject;

public class OneDriveTouchFeature implements Touch {

    private final OneDriveSession session;

    public OneDriveTouchFeature(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file, final TransferStatus status) throws BackgroundException {
        if(file.isRoot() || file.getParent().isRoot()) {
            throw new BackgroundException("Cannot create file here", "Create file in container");
        }

        // evaluating query
        StringBuilder builder = session.getBaseUrlStringBuilder();
        final Path parent = file.getParent();
        PathContainerService pathContainerService = new PathContainerService();
        session.resolveDriveQueryPath(parent, builder, pathContainerService);
        session.resolveChildrenPath(parent, builder, pathContainerService);

        final URL apiUrl = session.getUrl(builder);

        try {
            OneDriveJsonRequest request = new OneDriveJsonRequest(apiUrl, "POST");
            JsonObject requestObject = new JsonObject();
            requestObject.add("name", file.getName());
            requestObject.add("file", new JsonObject());
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
    public boolean isSupported(final Path workdir) {
        return true;
    }

    @Override
    public Touch withWriter(final Write writer) {
        return this;
    }
}
