package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Thumbnail;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import com.google.api.services.drive.model.File;

public class DriveThumbnailFeature implements Thumbnail {
    private static final Logger log = LogManager.getLogger(DriveThumbnailFeature.class);

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveThumbnailFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public InputStream thumbnail(final Path file, final int size) throws BackgroundException {
        try {
            final File f = session.getClient().files().get(fileid.getFileId(file))
                    .setFields("thumbnailLink")
                    .setSupportsTeamDrives(HostPreferencesFactory.get(session.getHost()).getBoolean("googledrive.teamdrive.enable"))
                    .execute();
            final String link = f.getThumbnailLink();
            if(StringUtils.isBlank(link)) {
                log.warn("No thumbnail available for file {}", file);
                throw new NotfoundException(file.getAbsolute());
            }
            // Short-lived URL must be fetched with credentialed request. Size of image returned defaults
            // to 220 pixels on the longest edge adjustable with the trailing size parameter
            final HttpGet request = new HttpGet(size > 0 ? link.replaceFirst("=s\\d+$", String.format("=s%d", size)) : link);
            final HttpResponse response = session.getHttpClient().execute(request);
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    return new HttpMethodReleaseInputStream(response);
                default:
                    throw new DefaultHttpResponseExceptionMappingService().map("Download {0} failed", new HttpResponseException(
                            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()), file);
            }
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Download {0} failed", e, file);
        }
    }
}
