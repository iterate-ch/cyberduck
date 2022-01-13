package ch.cyberduck.core.http;

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

import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.input.NullInputStream;
import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpMethodReleaseInputStream extends CountingInputStream {
    private static final Logger log = LoggerFactory.getLogger(HttpMethodReleaseInputStream.class);

    private final AtomicBoolean close = new AtomicBoolean();
    private final HttpResponse response;

    public HttpMethodReleaseInputStream(final HttpResponse response) throws IOException {
        this(response, new TransferStatus());
    }

    /**
     * Create a HTTP method release input Stream
     *
     * @param response The HTTP response to read from
     * @param status   Transfer Status to update content length from response entity
     * @throws IOException          If there is a problem reading from the response
     * @throws NullPointerException If the response has no message entity
     */
    public HttpMethodReleaseInputStream(final HttpResponse response, final TransferStatus status) throws IOException {
        super(null == response.getEntity() ? new NullInputStream(0L) : response.getEntity().getContent());
        this.response = response;
        if(null != response.getEntity()) {
            if(TransferStatus.UNKNOWN_LENGTH == response.getEntity().getContentLength()) {
                log.warn(String.format("Discard length in transfer status for unknown content length in response %s", response));
                // Decompressing entity with unknown content length
                status.setLength(TransferStatus.UNKNOWN_LENGTH);
            }
        }
    }

    /**
     * This will force close the connection if the content has not been fully consumed
     *
     * @throws IOException if an I/O error occurs
     * @see CloseableHttpResponse#close()
     * @see HttpConnection#shutdown()
     */
    @Override
    public void close() throws IOException {
        if(close.get()) {
            log.warn(String.format("Skip double close of stream %s", this));
            return;
        }
        try {
            if(response instanceof CloseableHttpResponse) {
                if(null == response.getEntity()) {
                    super.close();
                }
                else if(TransferStatus.UNKNOWN_LENGTH == response.getEntity().getContentLength() && TransferStatus.UNKNOWN_LENGTH == this.read()) {
                    // Fully consumed for unknown content length with decompressing HTTP entity
                    super.close();
                }
                else if(this.getByteCount() == response.getEntity().getContentLength()) {
                    // Fully consumed
                    super.close();
                }
                else {
                    log.warn("Abort connection for response '{}'", response);
                    // Close an HTTP response as quickly as possible, avoiding consuming
                    // response data unnecessarily though at the expense of making underlying
                    // connections unavailable for reuse.
                    // The response proxy will force close the connection.
                    ((CloseableHttpResponse) response).close();
                }
            }
            else {
                // Consume and close
                super.close();
            }
        }
        finally {
            close.set(true);
        }
    }
}
