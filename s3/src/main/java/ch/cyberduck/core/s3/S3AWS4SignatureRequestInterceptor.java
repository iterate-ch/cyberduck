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
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.ServiceUtils;
import org.jets3t.service.utils.SignatureUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.amazonaws.auth.internal.SignerConstants;

import static com.amazonaws.services.s3.Headers.S3_ALTERNATE_DATE;

public class S3AWS4SignatureRequestInterceptor implements HttpRequestInterceptor {
    private static final Logger log = LogManager.getLogger(S3Session.class);

    private final S3Session session;

    public S3AWS4SignatureRequestInterceptor(final S3Session session) {
        this.session = session;
    }

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws IOException {
        if(!session.getClient().isAuthenticatedConnection()) {
            log.warn("Skip authentication request {}", request);
            return;
        }
        final ProviderCredentials credentials = session.getClient().getProviderCredentials();
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
        String region = session.getClient().getRegionEndpointCache().getRegionForBucketName(bucketName);
        if(null == region) {
            final HttpHost host = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
            if(host != null) {
                try {
                    region = SignatureUtils.awsRegionForRequest(new URI(host.toURI()));
                    log.debug("Determined region {} from URI {}", region, host.toURI());
                }
                catch(URISyntaxException e) {
                    throw new IOException(e);
                }
            }
            if(region != null) {
                log.debug("Cache region {} for bucket {}", region, bucketName);
                session.getClient().getRegionEndpointCache().putRegionForBucketName(bucketName, region);
            }
        }
        if(null == region) {
            region = session.getHost().getRegion();
            log.debug("Determined region {} from {}", region, session.getHost());
        }
        if(null == region) {
            region = new HostPreferences(session.getHost()).getProperty("s3.location");
            log.debug("Determined region {} from defaults", region);
        }
        final HttpUriRequest message = (HttpUriRequest) request;
        String requestPayloadHexSHA256Hash =
                SignatureUtils.awsV4GetOrCalculatePayloadHash(message);
        message.setHeader(SignerConstants.X_AMZ_CONTENT_SHA256, requestPayloadHexSHA256Hash);
        // Generate AWS-flavoured ISO8601 timestamp string
        final String timestampISO8601 = message.getFirstHeader(S3_ALTERNATE_DATE).getValue();
        // Canonical request string
        final String canonicalRequestString = awsV4BuildCanonicalRequestString(uri,
                request.getRequestLine().getMethod(), this.getHeaders(request), requestPayloadHexSHA256Hash);
        // String to sign
        final String stringToSign = SignatureUtils.awsV4BuildStringToSign(
                session.getSignatureVersion().toString(), canonicalRequestString,
                timestampISO8601, region);
        // Signing key
        final byte[] signingKey = SignatureUtils.awsV4BuildSigningKey(
                credentials.getSecretKey(), timestampISO8601, region);
        // Request signature
        final String signature = ServiceUtils.toHex(ServiceUtils.hmacSHA256(
                signingKey, ServiceUtils.stringToBytes(stringToSign)));
        // Authorization header value
        final String authorizationHeaderValue =
                SignatureUtils.awsV4BuildAuthorizationHeaderValue(
                        credentials.getAccessKey(), signature,
                        session.getSignatureVersion().toString(), canonicalRequestString,
                        timestampISO8601, region);
        message.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
    }

    final class HttpHeaderFilter implements Predicate<Header> {
        @Override
        public boolean test(final Header header) {
            return !new HostPreferences(session.getHost()).getList("s3.signature.headers.exclude").stream()
                    .filter(s -> StringUtils.equalsIgnoreCase(s, header.getName())).findAny().isPresent();
        }
    }

    private Map<String, String> getHeaders(final HttpRequest request) {
        final Map<String, String> headers = new HashMap<>();
        for(Header header : Arrays.stream(request.getAllHeaders()).filter(new HttpHeaderFilter()).collect(Collectors.toList())) {
            headers.put(StringUtils.lowerCase(StringUtils.trim(header.getName())), StringUtils.trim(header.getValue()));
        }
        return headers;
    }

    public static String awsV4BuildCanonicalRequestString(
            URI uri, String httpMethod, Map<String, String> headersMap,
            String requestPayloadHexSha256Hash) {
        StringBuilder canonicalStringBuf = new StringBuilder();

        // HTTP Request method: GET, POST etc
        canonicalStringBuf
                .append(httpMethod)
                .append("\n");

        // Canonical URI: URI-encoded version of the absolute path
        String absolutePath = uri.getPath();
        if(absolutePath.isEmpty()) {
            canonicalStringBuf.append("/");
        }
        else {
            canonicalStringBuf.append(
                    SignatureUtils.awsV4EncodeURI(absolutePath, false));
        }
        canonicalStringBuf.append("\n");

        // Canonical query string
        String query = uri.getRawQuery();
        if(query == null || query.isEmpty()) {
            canonicalStringBuf.append("\n");
        }
        else {
            // Parse and sort query parameters and values from query string
            SortedMap<String, String> sortedQueryParameters =
                    new TreeMap<>();
            for(String paramPair : query.split("&")) {
                String[] paramNameValue = paramPair.split("=", 2);
                String name = paramNameValue[0];
                String value = "";
                if(paramNameValue.length > 1) {
                    value = paramNameValue[1];
                }
                // Add parameters to sorting map, URI-encoded appropriately
                sortedQueryParameters.put(
                        name,
                        value.replace("/", "%2F"));
            }
            // Add query parameters to canonical string
            boolean isPriorParam = false;
            for(Map.Entry<String, String> entry : sortedQueryParameters.entrySet()) {
                if(isPriorParam) {
                    canonicalStringBuf.append("&");
                }
                canonicalStringBuf
                        .append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
                isPriorParam = true;
            }
            canonicalStringBuf.append("\n");
        }

        // Canonical Headers
        SortedMap<String, String> sortedHeaders = new TreeMap<>(headersMap);
        sortedHeaders.remove(HttpHeaders.EXPECT.toLowerCase());
        for(Map.Entry<String, String> entry : sortedHeaders.entrySet()) {
            canonicalStringBuf
                    .append(entry.getKey())
                    .append(":")
                    .append(entry.getValue())
                    .append("\n");
        }
        canonicalStringBuf.append("\n");

        // Signed headers
        boolean isPriorSignedHeader = false;
        for(Map.Entry<String, String> entry : sortedHeaders.entrySet()) {
            if(isPriorSignedHeader) {
                canonicalStringBuf.append(";");
            }
            canonicalStringBuf.append(entry.getKey());
            isPriorSignedHeader = true;
        }
        canonicalStringBuf.append("\n");

        // Hashed Payload.
        canonicalStringBuf
                .append(requestPayloadHexSha256Hash);

        return canonicalStringBuf.toString();
    }
}
