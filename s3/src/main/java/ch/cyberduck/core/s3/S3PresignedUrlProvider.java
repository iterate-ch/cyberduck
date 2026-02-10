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

import java.net.URL;
import java.util.Date;

import com.amazonaws.HttpMethod;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import static ch.cyberduck.core.s3.S3CredentialsStrategy.toCredentialsProvider;

public class S3PresignedUrlProvider {

    private final Host host;
    private final HostPreferences preferences;

    public S3PresignedUrlProvider(final S3Session session) {
        this.host = session.getHost();
        this.preferences = HostPreferencesFactory.get(session.getHost());
    }

    /**
     * Generates a signed URL string that will grant access to an S3 resource (bucket or object) to whoever uses the URL
     * up until the time specified.
     *
     * @param bucketname the name of the bucket to include in the URL, must be a valid bucket name.
     * @param key        the name of the object to include in the URL, if null only the bucket name is used.
     * @param method     HTTP method
     * @param expiry     Milliseconds
     * @return a URL signed in such a way as to grant access to an S3 resource to whoever uses it.
     */
    public String create(final Credentials credentials, final String bucketname, final String region, final String key, final String method, final long expiry) {
        final AmazonS3 client = AmazonS3ClientBuilder.standard()
                .withDualstackEnabled(S3Session.isAwsHostname(host.getHostname()) && preferences.getBoolean("s3.endpoint.dualstack.enable"))
                .withPathStyleAccessEnabled(preferences.getBoolean("s3.bucket.virtualhost.disable"))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        S3Session.isAwsHostname(host.getHostname()) ?
                                RequestEntityRestStorageService.createRegionSpecificEndpoint(host, region) : host.getHostname(), region))
                .withCredentials(toCredentialsProvider(credentials)).build();
        final URL presigned = client.generatePresignedUrl(bucketname, key, new Date(expiry), HttpMethod.valueOf(method));
        // Allow for non-standard virtual directory paths on the server-side
        String context = host.getProtocol().getContext();
        if(StringUtils.isNotBlank(context) && !Scheme.isURL(context)) {
            context = PathNormalizer.normalize(context);
        }
        else {
            context = StringUtils.EMPTY;
        }
        return String.format("%s://%s%s%s?%s", presigned.getProtocol(), presigned.getAuthority(), context, presigned.getPath(), presigned.getQuery());
    }
}
