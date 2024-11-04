package ch.cyberduck.core.dav;

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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.VoidResponseHandler;

public class DAVReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(DAVReadFeature.class);

    private final DAVSession session;

    public DAVReadFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final List<Header> headers = new ArrayList<Header>(this.headers());
        if(status.isAppend()) {
            final HttpRange range = HttpRange.withStatus(status);
            final String header;
            if(TransferStatus.UNKNOWN_LENGTH == range.getEnd()) {
                header = String.format("bytes=%d-", range.getStart());
            }
            else {
                header = String.format("bytes=%d-%d", range.getStart(), range.getEnd());
            }
            log.debug("Add range header {} for file {}", header, file);
            headers.add(new BasicHeader(HttpHeaders.RANGE, header));
            // Disable compression
            headers.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "identity"));
        }
        try {
            final HttpRequestBase request = this.toRequest(file, status);
            for(Header header : headers) {
                request.addHeader(header);
            }
            final HttpResponse response = session.getClient().execute(request);
            final VoidResponseHandler handler = new VoidResponseHandler();
            try {
                handler.handleResponse(response);
                // Will abort the read when closed before EOF.
                final ContentLengthStatusInputStream stream = new ContentLengthStatusInputStream(new HttpMethodReleaseInputStream(response, status),
                        response.getEntity().getContentLength(),
                        response.getStatusLine().getStatusCode());
                if(status.isAppend()) {
                    if(stream.getCode() == HttpStatus.SC_OK) {
                        if(TransferStatus.UNKNOWN_LENGTH != status.getLength()) {
                            if(stream.getLength() != status.getLength()) {
                                log.warn("Range header not supported. Skipping {} bytes in file {}.", status.getOffset(), file);
                                stream.skip(status.getOffset());
                            }
                        }
                    }
                }
                return stream;
            }
            catch(IOException ex) {
                request.abort();
                throw ex;
            }
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    protected HttpRequestBase toRequest(final Path file, final TransferStatus status) throws BackgroundException {
        final StringBuilder resource = new StringBuilder(new DAVPathEncoder().encode(file));
        if(!status.getParameters().isEmpty()) {
            resource.append("?");
        }
        for(Map.Entry<String, String> parameter : status.getParameters().entrySet()) {
            if(!resource.toString().endsWith("?")) {
                resource.append("&");
            }
            resource.append(URIEncoder.encode(parameter.getKey()))
                    .append("=")
                    .append(URIEncoder.encode(parameter.getValue()));

        }
        return new HttpGet(resource.toString());
    }

    public Set<Header> headers() {
        return Collections.emptySet();
    }
}
