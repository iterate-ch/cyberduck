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

import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;

public class S3PresignedUrlProvider {
    private final S3Session session;

    public S3PresignedUrlProvider(final S3Session session) {
        this.session = session;
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
    public String create(final String secret, final String bucket, String region, final String key, final String method, final long expiry) {
        if(StringUtils.isBlank(region)) {
            // Only for AWS
            switch(session.getSignatureVersion()) {
                case AWS4HMACSHA256:
                    // Region is required for AWS4-HMAC-SHA256 signature
                    region = "us-east-1";
            }
        }
        return new RestS3Service(new AWSCredentials(StringUtils.strip(
            session.getHost().getCredentials().getUsername()
        ), StringUtils.strip(secret))) {
            @Override
            public String getEndpoint() {
                return session.getHost().getHostname();
            }

            @Override
            protected void initializeProxy(final HttpClientBuilder httpClientBuilder) {
                //
            }
        }.createSignedUrlUsingSignatureVersion(
                session.getSignatureVersion().toString(),
                region, method, bucket, key, null, null, expiry / 1000, false, true,
                new HostPreferences(session.getHost()).getBoolean("s3.bucket.virtualhost.disable"));
    }
}
