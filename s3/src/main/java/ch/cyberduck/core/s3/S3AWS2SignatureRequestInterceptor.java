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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import static com.amazonaws.services.s3.Headers.SECURITY_TOKEN;

public class S3AWS2SignatureRequestInterceptor implements HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(S3AWS2SignatureRequestInterceptor.class);

    private final S3Session session;

    public S3AWS2SignatureRequestInterceptor(final S3Session session) {
        this.session = session;
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws IOException {
        try {
            final Credentials credentials = session.getAuthentication().get();
            if(credentials.isAnonymousLogin()) {
                log.warn("Skip authentication request {}", request);
                return;
            }
            if(StringUtils.isNotBlank(credentials.getToken())) {
                request.setHeader(SECURITY_TOKEN, credentials.getToken());
            }
            final String bucketName;
            if(context.getAttribute("bucket") == null) {
                log.warn("No bucket name in context {}", context);
                bucketName = StringUtils.EMPTY;
            }
            else {
                bucketName = context.getAttribute("bucket").toString();
            }
            log.debug("Use bucket name {} from context", bucketName);
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
                    credentials.getTokens().getSecretAccessKey(), canonicalString);
            // Add encoded authorization to connection as HTTP Authorization header.
            final String authorizationString = session.getSignatureIdentifier() + " "
                    + credentials.getTokens().getAccessKeyId() + ":" + signedCanonical;
            request.setHeader(HttpHeaders.AUTHORIZATION, authorizationString);
        }
        catch(BackgroundException e) {
            log.warn("Error {} retrieving credentials", e.toString());
            throw new IOException(e);
        }
    }

    final class HttpHeaderFilter implements Predicate<Header> {
        @Override
        public boolean test(final Header header) {
            return HostPreferencesFactory.get(session.getHost()).getList("s3.signature.headers.exclude").stream()
                    .noneMatch(s -> StringUtils.equalsIgnoreCase(s, header.getName()));
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
