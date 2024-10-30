package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.RestUtils;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class S3AWS2SignatureRequestInterceptor implements HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(S3Session.class);

    private final S3Session session;

    public S3AWS2SignatureRequestInterceptor(final S3Session session) {
        this.session = session;
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws IOException {
        if(!session.getClient().isAuthenticatedConnection()) {
            log.warn(String.format("Skip authentication request %s", request));
            return;
        }
        final ProviderCredentials credentials = session.getClient().getProviderCredentials();
        final String bucketName;
        if(context.getAttribute("bucket") == null) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("No bucket name in context %s", context));
            }
            bucketName = StringUtils.EMPTY;
        }
        else {
            bucketName = context.getAttribute("bucket").toString();
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Use bucket name %s from context", bucketName));
        }
        final URI uri;
        try {
            uri = new URI(request.getRequestLine().getUri());
        }
        catch(URISyntaxException e) {
            throw new IOException(e);
        }
        String path = uri.getRawPath();
        // If the request specifies a bucket using the HTTP Host header (virtual hosted-style), append
        // the bucket name preceded by a "/"
        if(!StringUtils.startsWith(path, String.format("/%s", bucketName))) {
            path = String.format("/%s%s", bucketName, path);
        }
        final String queryString = uri.getRawQuery();
        if(StringUtils.isNotBlank(queryString)) {
            path += String.format("?%s", queryString);
        }
        // Generate a canonical string representing the operation.
        final String canonicalString = RestUtils.makeServiceCanonicalString(
                request.getRequestLine().getMethod(),
                path,
                this.getHeaders(request),
                null,
                session.getRestHeaderPrefix(),
                session.getClient().getResourceParameterNames());
        // Sign the canonical string.
        final String signedCanonical = ServiceUtils.signWithHmacSha1(
                credentials.getSecretKey(), canonicalString);
        // Add encoded authorization to connection as HTTP Authorization header.
        final String authorizationString = session.getSignatureIdentifier() + " "
                + credentials.getAccessKey() + ":" + signedCanonical;
        request.setHeader(HttpHeaders.AUTHORIZATION, authorizationString);
    }

    final class HttpHeaderFilter implements Predicate<Header> {
        @Override
        public boolean test(final Header header) {
            return !new HostPreferences(session.getHost()).getList("s3.signature.headers.exclude").stream()
                    .filter(s -> StringUtils.equalsIgnoreCase(s, header.getName())).findAny().isPresent();
        }
    }

    private Map<String, Object> getHeaders(final HttpRequest request) {
        final Map<String, Object> headers = new HashMap<>();
        for(Header header : Arrays.stream(request.getAllHeaders()).filter(new HttpHeaderFilter()).collect(Collectors.toList())) {
            headers.put(StringUtils.lowerCase(StringUtils.trim(header.getName())), StringUtils.trim(header.getValue()));
        }
        return headers;
    }
}
