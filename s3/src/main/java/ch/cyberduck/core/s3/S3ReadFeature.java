package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class S3ReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(S3ReadFeature.class);

    private final PathContainerService containerService;
    private final S3Session session;

    public S3ReadFeature(final S3Session session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(file.getType().contains(Path.Type.upload)) {
                return new NullInputStream(0L);
            }
            final HttpRange range = HttpRange.withStatus(status);
            final RequestEntityRestStorageService client = session.getClient();
            final Map<String, Object> requestHeaders = new HashMap<>();
            final Map<String, String> requestParameters = new HashMap<>();
            if(file.attributes().getVersionId() != null) {
                requestParameters.put("versionId", file.attributes().getVersionId());
            }
            if(status.isAppend()) {
                final String header;
                if(TransferStatus.UNKNOWN_LENGTH == range.getEnd()) {
                    header = String.format("bytes=%d-", range.getStart());
                }
                else {
                    header = String.format("bytes=%d-%d", range.getStart(), range.getEnd());
                }
                log.debug("Add range header {} for file {}", header, file);
                requestHeaders.put(HttpHeaders.RANGE, header);
            }
            final Path bucket = containerService.getContainer(file);
            final HttpResponse response = client.performRestGet(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(),
                    containerService.getKey(file), requestParameters, requestHeaders, new int[]{HttpStatus.SC_PARTIAL_CONTENT, HttpStatus.SC_OK});
            return new HttpMethodReleaseInputStream(response, status);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }
}
