package ch.cyberduck.core.onedrive.features;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Thumbnail;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;

import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveRequest;
import org.nuxeo.onedrive.client.OneDriveResponse;
import org.nuxeo.onedrive.client.OneDriveRuntimeException;
import org.nuxeo.onedrive.client.URLTemplate;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class GraphThumbnailFeature implements Thumbnail {

    private final GraphSession session;
    private final GraphFileIdProvider fileid;

    public GraphThumbnailFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public InputStream thumbnail(final Path file, final int size) throws BackgroundException {
        try {
            final DriveItem target = session.getItem(file);
            // Custom thumbnail size fitting inside a box of the requested size maintaining aspect ratio
            final URL url = new URLTemplate(target.getAction(String.format("/thumbnails/0/c%1$dx%1$d/content", size)))
                    .build(target.getApi().getBaseURL());
            final OneDriveRequest request = new OneDriveRequest(url, "GET");
            final OneDriveResponse response = request.sendRequest(target.getApi().getExecutor());
            return response.getContent();
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(OneDriveRuntimeException e) {
            throw new GraphExceptionMappingService(fileid).map("Download {0} failed", e.getCause(), file);
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return file.isFile() && !file.isPlaceholder();
    }
}
