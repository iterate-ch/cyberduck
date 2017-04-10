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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.date.ISO8601DateParser;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveJsonRequest;
import org.nuxeo.onedrive.client.OneDriveJsonResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class OneDriveAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(OneDriveAttributesFinderFeature.class);

    private final OneDriveSession session;

    private final ISO8601DateParser dateParser = new ISO8601DateParser();

    public OneDriveAttributesFinderFeature(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            final PathAttributes attributes = new PathAttributes();

            // Evaluating query
            final OneDriveUrlBuilder builder = new OneDriveUrlBuilder(session)
                    .resolveDriveQueryPath(file);
            OneDriveJsonRequest request = new OneDriveJsonRequest(builder.build(), "GET");
            OneDriveJsonResponse response = request.sendRequest(session.getClient().getExecutor());
            final JsonObject jsonObject = response.getContent();

            attributes.setVersionId(jsonObject.get("id").asString());

            JsonValue driveType = jsonObject.get("driveType");
            if(driveType != null && !driveType.isNull()) {
                // this is drive object we are on /drives hierarchy
            }
            else {
                attributes.setETag(jsonObject.get("eTag").asString());
                attributes.setSize(jsonObject.get("size").asLong());
                try {
                    attributes.setLink(new DescriptiveUrl(new URI(jsonObject.get("webUrl").asString()), DescriptiveUrl.Type.http));
                }
                catch(URISyntaxException e) {
                    log.warn(String.format("Cannot set link. Web URL returned %s", jsonObject.get("webUrl")), e);
                }
                try {
                    final Date createdDateTimeValue = dateParser.parse(jsonObject.get("createdDateTime").asString());
                    attributes.setCreationDate(createdDateTimeValue.getTime());
                }
                catch(InvalidDateException e) {
                    log.warn(String.format("Cannot parse Created Date Time. createdDateTime on Item returned %s", jsonObject.get("createdDateTime")), e);
                }
                try {
                    final Date lastModifiedDateTime = dateParser.parse(jsonObject.get("lastModifiedDateTime").asString());
                    attributes.setCreationDate(lastModifiedDateTime.getTime());
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
                        attributes.setCreationDate(dateParser.parse(createdDateTimeValue).getTime());
                    }
                    catch(InvalidDateException e) {
                        log.warn(String.format("Cannot parse Created Date Time. createdDateTime on FilesystemInfo Facet returned %s", jsonObject.get("createdDateTime")), e);
                    }
                    try {
                        attributes.setModificationDate(dateParser.parse(lastModifiedDateTimeValue).getTime());
                    }
                    catch(InvalidDateException e) {
                        log.warn(String.format("Cannot parse Last Modified Date Time. lastModifiedDateTime on FilesystemInfo Facet returned %s", jsonObject.get("lastModifiedDateTime")), e);
                    }
                }
            }
            return attributes;
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        return this;
    }
}
