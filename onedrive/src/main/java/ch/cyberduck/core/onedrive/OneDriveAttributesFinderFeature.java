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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.date.ISO8601DateParser;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;

import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

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
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }

        PathAttributes pathAttributes = new PathAttributes();

        // evaluating query
        StringBuilder builder = session.getBaseUrlStringBuilder();

        PathContainerService pathContainerService = new PathContainerService();
        session.resolveDriveQueryPath(file, builder, pathContainerService);
        /*if(pathContainerService.isContainer(file)) {
            builder.append("/root");
        }*/

        final URL apiUrl = session.getUrl(builder);
        final JsonObject jsonObject = session.getSimpleResult(apiUrl);

        JsonValue driveType = jsonObject.get("driveType");
        if(driveType != null && !driveType.isNull()) {
            // this is drive object we are on /drives hierarchy
        }
        else {
            pathAttributes.setVersionId(jsonObject.get("id").asString());
            pathAttributes.setETag(jsonObject.get("eTag").asString());
            pathAttributes.setSize(jsonObject.get("size").asLong());
            try {
                pathAttributes.setLink(new DescriptiveUrl(new URI(jsonObject.get("webUrl").asString()), DescriptiveUrl.Type.http));
            }
            catch(URISyntaxException e) {
                log.warn(String.format("Cannot set link. Web URL returned %s", jsonObject.get("webUrl")), e);
            }

            ISO8601DateParser dateParser = new ISO8601DateParser();
            try {
                final Date createdDateTimeValue = dateParser.parse(jsonObject.get("createdDateTime").asString());
                pathAttributes.setCreationDate(createdDateTimeValue.getTime());
            }
            catch(InvalidDateException e) {
                log.warn(String.format("Cannot parse Created Date Time. createdDateTime on Item returned %s", jsonObject.get("createdDateTime")), e);
            }
            try {
                final Date lastModifiedDateTime = dateParser.parse(jsonObject.get("lastModifiedDateTime").asString());
                pathAttributes.setCreationDate(lastModifiedDateTime.getTime());
            }
            catch(InvalidDateException e) {
                log.warn(String.format("Cannot parse Last Modified Date Time. lastModifiedDateTime on Item returned %s", jsonObject.get("lastModifiedDateTime")), e);
            }

            final JsonValue fileValue = jsonObject.get("file");
            final JsonValue folderValue = jsonObject.get("folder");
            final JsonValue filesystemValue = jsonObject.get("fileSystemInfo");

            if(fileValue != null && !fileValue.isNull()) {
                final JsonObject fileObject = fileValue.asObject();
            }
            else if(folderValue != null && !folderValue.isNull()) {
                final JsonObject folderObject = folderValue.asObject();
            }

            if(filesystemValue != null && !filesystemValue.isNull()) {
                final JsonObject filesystemObject = filesystemValue.asObject();
                String createdDateTimeValue = filesystemObject.get("createdDateTime").asString();
                String lastModifiedDateTimeValue = filesystemObject.get("lastModifiedDateTime").asString();

                try {
                    pathAttributes.setCreationDate(dateParser.parse(createdDateTimeValue).getTime());
                }
                catch(InvalidDateException e) {
                    log.warn(String.format("Cannot parse Created Date Time. createdDateTime on FilesystemInfo Facet returned %s", jsonObject.get("createdDateTime")), e);
                }
                try {
                    pathAttributes.setModificationDate(dateParser.parse(lastModifiedDateTimeValue).getTime());
                }
                catch(InvalidDateException e) {
                    log.warn(String.format("Cannot parse Last Modified Date Time. lastModifiedDateTime on FilesystemInfo Facet returned %s", jsonObject.get("lastModifiedDateTime")), e);
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
