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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.http.ExtendedHttpRequestRetryHandler;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.JetS3tRequestAuthorizer;
import org.jets3t.service.utils.ServiceUtils;
import org.jets3t.service.utils.SignatureUtils;

import java.io.IOException;
import java.net.URI;

public class S3HttpRequestRetryHandler extends ExtendedHttpRequestRetryHandler {
    private static final Logger log = LogManager.getLogger(S3HttpRequestRetryHandler.class);

    private static final int MAX_RETRIES = 1;

    private final Host host;
    private final JetS3tRequestAuthorizer authorizer;

    public S3HttpRequestRetryHandler(final Host host, final JetS3tRequestAuthorizer authorizer, final int retryCount) {
        super(retryCount);
        this.host = host;
        this.authorizer = authorizer;
    }

    @Override
    public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context) {
        if(executionCount <= MAX_RETRIES) {
            if(super.retryRequest(exception, executionCount, context)) {
                final Object attribute = context.getAttribute(HttpCoreContext.HTTP_REQUEST);
                if(attribute instanceof HttpUriRequest) {
                    final HttpUriRequest request = (HttpUriRequest) attribute;
                    if(log.isWarnEnabled()) {
                        log.warn(String.format("Retrying request %s", request));
                    }
                    try {
                        final URI uri = URI.create(((HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST)).toURI());
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Lookup region for URI %s", uri));
                        }
                        final String region = SignatureUtils.awsRegionForRequest(uri);
                        if(null == region) {
                            log.warn(String.format("Failure to determine region in URI %s", uri));
                            return false;
                        }
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Determined region %s from URI %s", region, uri));
                        }
                        final String bucketName = ServiceUtils.findBucketNameInHostOrPath(uri,
                                RequestEntityRestStorageService.createRegionSpecificEndpoint(host, region));
                        if(null == bucketName) {
                            return false;
                        }
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Determined bucket %s from URI %s", bucketName, uri));
                        }
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Authorize request %s", request));
                        }
                        // Build the authorization string for the method.
                        authorizer.authorizeHttpRequest(bucketName, request, context, null);
                        if(log.isWarnEnabled()) {
                            log.warn(String.format("Retrying request %s", request));
                        }
                        return true;
                    }
                    catch(ServiceException e) {
                        log.warn("Unable to generate updated authorization string for retried request", e);
                    }
                }
            }
        }
        return false;
    }
}
