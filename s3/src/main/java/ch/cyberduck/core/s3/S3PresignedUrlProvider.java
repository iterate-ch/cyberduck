package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSSessionCredentials;

public class S3PresignedUrlProvider {
    private static final Logger log = LogManager.getLogger(S3PresignedUrlProvider.class);

    private final S3Session session;
    private final HostPreferences preferences;

    public S3PresignedUrlProvider(final S3Session session) {
        this.session = session;
        this.preferences = HostPreferencesFactory.get(session.getHost());
    }

    /**
     * Generates a signed URL string that will grant access to an S3 resource (bucket or object) to whoever uses the URL
     * up until the time specified.
     *
     * @param bucket the name of the bucket to include in the URL, must be a valid bucket name.
     * @param key    the name of the object to include in the URL, if null only the bucket name is used.
     * @param method HTTP method
     * @param expiry Milliseconds
     * @return a URL signed in such a way as to grant access to an S3 resource to whoever uses it.
     */
    public String create(final Credentials credentials, final String bucket, final String region, final String key, final String method, final long expiry) {
        final Host bookmark = session.getHost();
        return new RestS3Service(credentials.getTokens().validate() ?
                new AWSSessionCredentials(credentials.getTokens().getAccessKeyId(), credentials.getTokens().getSecretAccessKey(),
                        credentials.getTokens().getSessionToken()) :
                new AWSCredentials(credentials.getTokens().getAccessKeyId(), credentials.getTokens().getSecretAccessKey())) {
            @Override
            public String getEndpoint() {
                if(S3Session.isAwsHostname(bookmark.getHostname())) {
                    final String endpoint = preferences.getBoolean("s3.endpoint.dualstack.enable")
                            ? preferences.getProperty("s3.endpoint.format.ipv6") : preferences.getProperty("s3.endpoint.format.ipv4");
                    log.debug("Apply region {} to endpoint {}", region, endpoint);
                    return String.format(endpoint, region);
                }
                return bookmark.getHostname();
            }

            @Override
            protected String getVirtualPath() {
                final String context = bookmark.getProtocol().getContext();
                if(StringUtils.isNotBlank(context) && !Scheme.isURL(context)) {
                    return PathNormalizer.normalize(context);
                }
                return StringUtils.EMPTY;
            }

            @Override
            protected void initializeProxy(final HttpClientBuilder httpClientBuilder) {
                //
            }
        }.createSignedUrlUsingSignatureVersion(
                session.getSignatureVersion().toString(),
                region, method, bucket, key, null, null, expiry / 1000, false, true,
                preferences.getBoolean("s3.bucket.virtualhost.disable"));
    }
}
