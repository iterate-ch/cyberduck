package ch.cyberduck.core.sts;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.http.HttpRateLimiter;
import ch.cyberduck.core.s3.S3Session;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.security.AWSSessionCredentials;

public class STSSecurityTokenHeaderHttpRequestInterceptor implements HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(STSSecurityTokenHeaderHttpRequestInterceptor.class);

    private final S3Session session;
    private static final String securityTokenHeaderKey = "x-amz-security-token";

    public STSSecurityTokenHeaderHttpRequestInterceptor(S3Session session) {
        this.session = session;
    }

    @Override
    public void process(final org.apache.http.HttpRequest request, final HttpContext context) {
        request.setHeader(securityTokenHeaderKey, ((AWSSessionCredentials) session.getClient().getProviderCredentials()).getSessionToken());
    }
}
