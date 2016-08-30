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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.io.ContentLengthInputStream;

public class DAVReadFeature implements Read {
    private static final Logger log = Logger.getLogger(DAVReadFeature.class);

    private DAVSession session;

    public DAVReadFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        final List<Header> headers = new ArrayList<Header>();
        if(status.isAppend()) {
            final HttpRange range = HttpRange.withStatus(status);
            final String header;
            if(-1 == range.getEnd()) {
                header = String.format("bytes=%d-", range.getStart());
            }
            else {
                header = String.format("bytes=%d-%d", range.getStart(), range.getEnd());
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Add range header %s for file %s", header, file));
            }
            headers.add(new BasicHeader(HttpHeaders.RANGE, header));
            // Disable compression
            headers.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "identity"));
        }
        try {
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
            final ContentLengthInputStream stream = session.getClient().get(resource.toString(), headers);
            if(status.isAppend()) {
                if(-1 == status.getLength()) {
                    if(stream.getLength() == file.attributes().getSize()) {
                        log.warn(String.format("Range header not supported. Skipping %d bytes in file %s.", status.getOffset(), file));
                        stream.skip(status.getOffset());
                    }
                }
                else {
                    if(stream.getLength() != status.getLength()) {
                        log.warn(String.format("Range header not supported. Skipping %d bytes in file %s.", status.getOffset(), file));
                        stream.skip(status.getOffset());
                    }
                }
            }
            return stream;
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) {
        return true;
    }
}
