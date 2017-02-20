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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;

import java.net.URL;
import java.util.EnumSet;
import java.util.Iterator;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class OneDriveListService implements ListService {
    private static final Logger log = Logger.getLogger(OneDriveListService.class);

    private final OneDriveSession session;

    public OneDriveListService(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();

        // evaluating query
        StringBuilder builder = session.getBaseUrlStringBuilder();

        PathContainerService pathContainerService = new PathContainerService();
        session.resolveDriveQueryPath(directory, builder, pathContainerService);
        session.resolveChildrenPath(directory, builder, pathContainerService);

        final URL apiUrl = session.getUrl(builder);
        Iterator<JsonObject> iterator = iterator = new JsonObjectIteratorPort(session.getClient(), apiUrl);

        try {
            log.info(String.format("Querying OneDrive API with %s", apiUrl));
            while(iterator.hasNext()) {
                try {
                    final String name;
                    final EnumSet<AbstractPath.Type> type;

                    JsonObject jsonObject = iterator.next();

                    JsonValue driveType = jsonObject.get("driveType");
                    if(driveType != null && !driveType.isNull()) {
                        // this is drive object we are on /drives hierarchy
                        name = jsonObject.get("id").asString(); // this may not fail
                        type = EnumSet.of(AbstractPath.Type.volume, AbstractPath.Type.directory);
                    }
                    else {
                        // try evaluating
                        JsonValue nameValue = jsonObject.get("name");
                        if(nameValue == null || nameValue.isNull() || !nameValue.isString()) {
                            // got null name (not found) or empty name (should not happen)
                            continue;
                        }
                        name = nameValue.asString();

                        JsonValue fileValue = jsonObject.get("file");
                        JsonValue folderValue = jsonObject.get("folder");
                        if(fileValue != null && !fileValue.isNull()) {
                            type = EnumSet.of(AbstractPath.Type.file);
                        }
                        else if(folderValue != null && !folderValue.isNull()) {
                            type = EnumSet.of(AbstractPath.Type.directory);
                        }
                        else {
                            // if everything else fails: ignore and continue
                            continue;
                        }
                    }

                    children.add(new Path(directory, name, type));
                }
                catch(OneDriveRuntimeException e) { // this catches iterator.next() whicht may not cause hasNext() to fail
                    continue; // silent ignore any OneDriveRuntimeException in next(). Might redirect to log!
                }
            }
        }
        catch(OneDriveRuntimeException e) { // this catches iterator.hasNext() which in return should fail fast
            throw new BackgroundException(e);
        }

        return children;
    }
}
