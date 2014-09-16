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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.ETagResponseHandler;

/**
 * @version $Id$
 */
public class DAVWriteFeature extends AbstractHttpWriteFeature<String> implements Write {

    private DAVSession session;

    /**
     * Use Expect directive
     */
    private boolean expect;

    public DAVWriteFeature(final DAVSession session) {
        this(session, Preferences.instance().getBoolean("webdav.expect-continue"));
    }

    public DAVWriteFeature(final DAVSession session, final boolean expect) {
        super(session);
        this.session = session;
        this.expect = expect;
    }

    @Override
    public ResponseOutputStream<String> write(final Path file, final TransferStatus status) throws BackgroundException {
        final Map<String, String> headers = new HashMap<String, String>();
        if(status.isAppend()) {
            headers.put(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d",
                            status.getCurrent(), status.getLength() - 1, status.getLength())
            );
        }
        if(expect) {
            headers.put(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
        }
        return this.write(file, headers, status);
    }

    private ResponseOutputStream<String> write(final Path file, final Map<String, String> headers, final TransferStatus status)
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
                            decorate(file, headers, status), new ETagResponseHandler());
                }
                catch(SardineException e) {
                    throw new DAVExceptionMappingService().map("Upload failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength() - status.getCurrent();
            }
        };
        return this.write(file, status, command);
    }

    protected Map<String, String> decorate(final Path file, final Map<String, String> headers,
                                           final TransferStatus status) throws BackgroundException {
        return headers;
    }

    @Override
    public boolean temporary() {
        return true;
    }
}
