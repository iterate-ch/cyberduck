package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 *
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Preferences;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.JetS3tRequestAuthorizer;
import org.jets3t.service.io.UnrecoverableIOException;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @version $Id$
 */
public class S3HttpREquestRetryHandler extends DefaultHttpRequestRetryHandler {
    private static final Logger log = Logger.getLogger(S3HttpREquestRetryHandler.class);

    private final JetS3tRequestAuthorizer authorizer;

    public S3HttpREquestRetryHandler(final JetS3tRequestAuthorizer authorizer) {
        this(authorizer, Preferences.instance().getInteger("connection.retry"));
    }

    public S3HttpREquestRetryHandler(final JetS3tRequestAuthorizer authorizer, final int retryCount) {
        super(retryCount, false,
                Arrays.asList(
                        UnrecoverableIOException.class,
                        InterruptedIOException.class,
                        UnknownHostException.class,
                        ConnectException.class,
                        // Not providing SSLException.class, because broken pipe failures are wrapped in SSL Exceptions.
                        // "Broken pipe".equals(ExceptionUtils.getRootCause(failure).getMessage())
                        SSLHandshakeException.class)
        );
        this.authorizer = authorizer;
    }

    @Override
    public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context) {
        if(super.retryRequest(exception, executionCount, context)) {
            final Object attribute = context.getAttribute(HttpCoreContext.HTTP_REQUEST);
            if(attribute instanceof HttpUriRequest) {
                final HttpUriRequest method = (HttpUriRequest) attribute;
                log.warn(String.format("Retrying request %s", method));
                try {
                    // Build the authorization string for the method.
                    authorizer.authorizeHttpRequest(method, context);
                    return true;
                }
                catch(ServiceException e) {
                    log.warn("Unable to generate updated authorization string for retried request", e);
                }
            }
        }
        return false;
    }
}
