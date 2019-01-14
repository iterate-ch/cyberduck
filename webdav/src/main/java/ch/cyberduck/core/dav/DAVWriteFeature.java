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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.ETagResponseHandler;

public class DAVWriteFeature extends AbstractHttpWriteFeature<String> implements Write<String> {
    private static final Logger log = Logger.getLogger(DAVWriteFeature.class);

    private final DAVSession session;

    /**
     * Use Expect directive
     */
    private final boolean expect;

    public DAVWriteFeature(final DAVSession session) {
        this(session, PreferencesFactory.get().getBoolean("webdav.expect-continue"));
    }

    public DAVWriteFeature(final DAVSession session, final boolean expect) {
        super(session);
        this.session = session;
        this.expect = expect;
    }

    public DAVWriteFeature(final DAVSession session, final Find finder, final AttributesFinder attributes, final boolean expect) {
        super(finder, attributes);
        this.session = session;
        this.expect = expect;
    }

    @Override
    public HttpResponseOutputStream<String> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final List<Header> headers = new ArrayList<Header>();
        if(status.isAppend()) {
            final HttpRange range = HttpRange.withStatus(status);
            if(-1L == range.getEnd()) {
                throw new UnsupportedException("Content-Range with unknown file size is not supported");
            }
            else {
                // Content-Range entity-header is sent with a partial entity-body to specify where
                // in the full entity-body the partial body should be applied.
                final String header = String.format("bytes %d-%d/%d", range.getStart(), range.getEnd(),
                    status.getOffset() + status.getLength());
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Add range header %s for file %s", header, file));
                }
                headers.add(new BasicHeader(HttpHeaders.CONTENT_RANGE, header));
            }
        }
        if(expect) {
            if(status.getLength() > 0L) {
                headers.add(new BasicHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE));
            }
        }
        if(status.getLockId() != null) {
            // Indicate that the client has knowledge of that state token
            headers.add(new BasicHeader(HttpHeaders.IF, String.format("(<%s>)", status.getLockId())));
        }
        return this.write(file, headers, status);
    }

    private HttpResponseOutputStream<String> write(final Path file, final List<Header> headers, final TransferStatus status)
        throws BackgroundException {
        // Submit store call to background thread
        final DelayedHttpEntityCallable<String> command = new DelayedHttpEntityCallable<String>() {
            /**
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public String call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    return session.getClient().put(new DAVPathEncoder().encode(file), entity,
                        headers, new ETagResponseHandler());
                }
                catch(SardineException e) {
                    throw new DAVExceptionMappingService().map("Upload {0} failed", e, file);
                }
                catch(IOException e) {
                    throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }

    @Override
    public boolean temporary() {
        return true;
    }

    @Override
    public boolean random() {
        return true;
    }
}
