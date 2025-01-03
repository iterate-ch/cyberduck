package ch.cyberduck.core.restore;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.auth.AWSCredentialsConfigurator;
import ch.cyberduck.core.aws.AmazonServiceExceptionMappingService;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Restore;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GlacierJobParameters;
import com.amazonaws.services.s3.model.RestoreObjectRequest;

public class Glacier implements Restore {
    private static final Logger log = LogManager.getLogger(Glacier.class);

    private final S3Session session;
    private final ClientConfiguration configuration;
    private final Location location;

    public Glacier(final S3Session session, final Location location, final X509TrustManager trust, final X509KeyManager key) {
        this.session = session;
        this.location = location;
        final Host bookmark = session.getHost();
        this.configuration = new CustomClientConfiguration(bookmark,
                new ThreadLocalHostnameDelegatingTrustManager(trust, bookmark.getHostname()), key);
    }

    /**
     * Objects in the GLACIER and DEEP_ARCHIVE storage classes are archived. To access an archived object, you must
     * first initiate a restore request. This restores a temporary copy of the archived object. In a restore request,
     * you specify the number of days that you want the restored copy to exist. After the specified period, Amazon S3
     * deletes the temporary copy but the object remains archived in the GLACIER or DEEP_ARCHIVE storage class that
     * object was restored from.
     *
     * @param file   Archived file
     * @param prompt Callback
     */
    @Override
    public void restore(final Path file, final LoginCallback prompt) throws BackgroundException {
        final Path container = session.getFeature(PathContainerService.class).getContainer(file);
        try {
            try {
                final AmazonS3 client = client(container);
                // Standard - S3 Standard retrievals allow you to access any of your archived objects within several hours.
                // This is the default option for the GLACIER and DEEP_ARCHIVE retrieval requests that do not specify
                // the retrieval option. S3 Standard retrievals typically complete within 3-5 hours from the GLACIER
                // storage class and typically complete within 12 hours from the DEEP_ARCHIVE storage class.
                client.restoreObjectV2(new RestoreObjectRequest(container.getName(), session.getFeature(PathContainerService.class).getKey(file))
                    // To restore a specific object version, you can provide a version ID. If you don't provide a version ID, Amazon S3 restores the current version.
                    .withVersionId(file.attributes().getVersionId())
                    .withExpirationInDays(new HostPreferences(session.getHost()).getInteger("s3.glacier.restore.expiration.days"))
                    .withGlacierJobParameters(new GlacierJobParameters().withTier(new HostPreferences(session.getHost()).getProperty("s3.glacier.restore.tier")))
                );
                // 200 Reply if already restored
            }
            catch(AmazonClientException e) {
                throw new AmazonServiceExceptionMappingService().map("Failure to write attributes of {0}", e, file);
            }
        }
        catch(ConflictException e) {
            // 409 when restore is in progress
            log.warn("Restore for {} already in progress {}", file, e);
        }
    }

    @Override
    public boolean isRestorable(final Path file) {
        return StringUtils.equals("GLACIER", file.attributes().getStorageClass()) ||
            StringUtils.equals("DEEP_ARCHIVE", file.attributes().getStorageClass());
    }

    private AmazonS3 client(final Path container) throws BackgroundException {
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withCredentials(AWSCredentialsConfigurator.toAWSCredentialsProvider(session.getClient().getProviderCredentials()))
            .withClientConfiguration(configuration);
        if(session.getClient().isAuthenticatedConnection()) {
            builder.withCredentials(AWSCredentialsConfigurator.toAWSCredentialsProvider(session.getClient().getProviderCredentials()));
        }
        final Location.Name region = this.getRegion(container);
        if(S3Session.isAwsHostname(session.getHost().getHostname(), false)) {
            if(Location.unknown.equals(region)) {
                builder.withRegion(Regions.DEFAULT_REGION);
            }
            else {
                builder.withRegion(region.getIdentifier());
            }
        }
        else {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                new HostUrlProvider(false).get(session.getHost()), region.getIdentifier()));
        }
        return builder.build();
    }

    protected Location.Name getRegion(final Path container) throws BackgroundException {
        return location.getLocation(container);
    }
}
