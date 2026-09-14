package ch.cyberduck.core.box;

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
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Retrieve a thumbnail preview image for a file generated on the server.
 *
 * @see <a href="https://developer.box.com/reference/get-files-id-thumbnail-id/">Get file thumbnail</a>
 */
public class BoxThumbnailFeature implements Thumbnail {
    private static final Logger log = LogManager.getLogger(BoxThumbnailFeature.class);

    /**
     * Available dimensions for the longest edge of a <code>png</code> thumbnail
     */
    private static final int[] DIMENSIONS = {32, 64, 128, 256};

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxThumbnailFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public InputStream thumbnail(final Path file, final int size) throws BackgroundException {
        try {
            final BoxApiClient client = new BoxApiClient(session.getClient());
            final int dimension = toDimension(size);
            final HttpGet request = new HttpGet(String.format("%s/files/%s/thumbnail.png?min_height=%d&min_width=%d",
                    client.getBasePath(), fileid.getFileId(file), dimension, dimension));
            final CloseableHttpResponse response = session.getClient().execute(request);
            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    return new HttpMethodReleaseInputStream(response);
                case HttpStatus.SC_ACCEPTED:
                    // Thumbnail is not yet available on the server. A placeholder is offered in the Location
                    // header but generation is asynchronous and the caller is expected to retry later
                    EntityUtils.consumeQuietly(response.getEntity());
                    log.warn("No thumbnail available yet for file {}", file);
                    throw new NotfoundException(file.getAbsolute());
                default:
                    EntityUtils.consumeQuietly(response.getEntity());
                    throw new DefaultHttpResponseExceptionMappingService().map("Download {0} failed", new HttpResponseException(
                            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()), file);
            }
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    /**
     * @return Smallest available dimension greater than or equal to the requested size clamped to the range
     * supported by the API
     */
    protected static int toDimension(final int size) {
        for(final int dimension : DIMENSIONS) {
            if(size <= dimension) {
                return dimension;
            }
        }
        return DIMENSIONS[DIMENSIONS.length - 1];
    }
}
