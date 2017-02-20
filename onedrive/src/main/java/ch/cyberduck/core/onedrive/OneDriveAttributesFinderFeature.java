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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveJsonRequest;
import org.nuxeo.onedrive.client.OneDriveJsonResponse;

import java.net.URL;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class OneDriveAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(OneDriveAttributesFinderFeature.class);

    private final OneDriveSession session;

    public OneDriveAttributesFinderFeature(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        PathAttributes pathAttributes = new PathAttributes();

        // evaluating query
        StringBuilder builder = session.getBaseUrlStringBuilder();

        PathContainerService pathContainerService = new PathContainerService();
        session.resolveDriveQueryPath(file, builder, pathContainerService);
        /*if(pathContainerService.isContainer(file)) {
            builder.append("/root");
        }*/

        final JsonObject jsonObject;
        final URL apiUrl = session.getUrl(builder);
        try {
            OneDriveJsonRequest request = new OneDriveJsonRequest(session.getClient(), apiUrl, "GET");
            OneDriveJsonResponse response = request.send();
            jsonObject = response.getContent();
        }
        catch(OneDriveAPIException e) {
            throw new BackgroundException(e);
        }

        JsonValue driveType = jsonObject.get("driveType");
        if(driveType != null && !driveType.isNull()) {
            // this is drive object we are on /drives hierarchy

        }
        else {
            // try evaluating
            JsonValue nameValue = jsonObject.get("name");
            if(!(nameValue == null || nameValue.isNull() || !nameValue.isString())) {
                // got null name (not found) or empty name (should not happen)
                final JsonValue fileValue = jsonObject.get("file");
                final JsonValue folderValue = jsonObject.get("folder");
                final JsonValue filesystemValue = jsonObject.get("filesysteminfo");

                if(fileValue != null && !fileValue.isNull()) {
                    final JsonObject fileObject = fileValue.asObject();

                }
                else if(folderValue != null && !folderValue.isNull()) {
                    final JsonObject folderObject = folderValue.asObject();
                }

                if(filesystemValue != null && !filesystemValue.isNull()) {
                    final JsonObject filesystemObject = filesystemValue.asObject();
                }
            }
        }

        return pathAttributes;
    }

    @Override
    public AttributesFinder withCache(final PathCache cache) {
        return this;
    }
}
