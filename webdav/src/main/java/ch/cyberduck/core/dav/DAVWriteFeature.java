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
import ch.cyberduck.core.VoidAttributesAdapter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.github.sardine.impl.SardineException;
import com.github.sardine.impl.handler.ETagResponseHandler;

public class DAVWriteFeature extends AbstractHttpWriteFeature<Void> implements Write<Void> {
    private static final Logger log = LogManager.getLogger(DAVWriteFeature.class);

    private final DAVSession session;

    /**
     * Use Expect directive
     */
    private final boolean expect;

    public DAVWriteFeature(final DAVSession session) {
        this(session, new HostPreferences(session.getHost()).getBoolean("webdav.expect-continue"));
    }

    public DAVWriteFeature(final DAVSession session, final boolean expect) {
        super(new VoidAttributesAdapter());
        this.session = session;
        this.expect = expect;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            return this.write(file, this.toHeaders(file, status, expect), status);
        }
        catch(ConflictException e) {
            if(expect) {
                if(null != status.getLockId()) {
                    // Handle 412 Precondition Failed with expired token
                    log.warn(String.format("Retry failure %s with lock id %s removed", e, status.getLockId()));
                    return this.write(file, this.toHeaders(file, status.withLockId(null), expect), status);
                }
            }
            throw e;
        }
        catch(InteroperabilityException e) {
            if(expect) {
                // Handle 417 Expectation Failed
                log.warn(String.format("Retry failure %s with Expect: Continue removed", e));
                return this.write(file, this.toHeaders(file, status.withLockId(null), false), status);
            }
            throw e;
        }
    }

    protected List<Header> toHeaders(final Path file, final TransferStatus status, final boolean expectdirective) throws UnsupportedException {
        final List<Header> headers = new ArrayList<>();
        if(status.isAppend()) {
            if(status.getLength() == TransferStatus.UNKNOWN_LENGTH) {
                throw new UnsupportedException("Content-Range with unknown file size is not supported");
            }
            final HttpRange range = HttpRange.withStatus(status);
            // Content-Range entity-header is sent with a partial entity-body to specify where
            // in the full entity-body the partial body should be applied.
            final String header = String.format("bytes %d-%d/%d", range.getStart(), range.getEnd(),
                    status.getOffset() + status.getLength());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Add range header %s for file %s", header, file));
            }
            headers.add(new BasicHeader(HttpHeaders.CONTENT_RANGE, header));
        }
        if(expectdirective) {
            if(status.getLength() > 0L) {
                headers.add(new BasicHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE));
            }
        }
        if(session.getFeature(Lock.class) != null && status.getLockId() != null) {
            // Indicate that the client has knowledge of that state token
            headers.add(new BasicHeader(HttpHeaders.IF, String.format("(<%s>)", status.getLockId())));
        }
        return headers;
    }

    private HttpResponseOutputStream<Void> write(final Path file, final List<Header> headers, final TransferStatus status)
            throws BackgroundException {
        // Submit store call to background thread
        final DelayedHttpEntityCallable<Void> command = new DelayedHttpEntityCallable<Void>(file) {
            /**
             * @return The ETag returned by the server for the uploaded object
             */
            @Override
            public Void call(final HttpEntity entity) throws BackgroundException {
                try {
                    session.getClient().put(new DAVPathEncoder().encode(file), entity,
                            headers, new ETagResponseHandler());
                    return null;
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
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.random);
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        if(status.getLength() == TransferStatus.UNKNOWN_LENGTH) {
            return new Append(false).withStatus(status);
        }
        return super.append(file, status);
    }
}
