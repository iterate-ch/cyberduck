package ch.cyberduck.core.restore;/*
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
import ch.cyberduck.core.KeychainLoginService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.LoginService;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.auth.AWSCredentialsConfigurator;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Restore;
import ch.cyberduck.core.iam.AmazonServiceExceptionMappingService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GlacierJobParameters;
import com.amazonaws.services.s3.model.RestoreObjectRequest;

public class Glacier implements Restore {
    private static final Logger log = Logger.getLogger(Glacier.class);

    private final PathContainerService containerService
        = new PathContainerService();

    private final Preferences preferences = PreferencesFactory.get();

    private final Host bookmark;
    private final ClientConfiguration configuration;
    private final Location locationFeature;

    public Glacier(final S3Session session, final X509TrustManager trust, final X509KeyManager key) {
        this.bookmark = session.getHost();
        this.configuration = new CustomClientConfiguration(bookmark,
            new ThreadLocalHostnameDelegatingTrustManager(trust, bookmark.getHostname()), key);
        this.locationFeature = session.getFeature(Location.class);
    }

    private interface Authenticated<T> extends Callable<T> {
        T call() throws BackgroundException;
    }

    private <T> T authenticated(final Authenticated<T> run, final LoginCallback prompt) throws BackgroundException {
        final LoginOptions options = new LoginOptions(bookmark.getProtocol())
            .usernamePlaceholder(LocaleFactory.localizedString("Access Key ID", "S3"))
            .passwordPlaceholder(LocaleFactory.localizedString("Secret Access Key", "S3"));
        try {
            final LoginService login = new KeychainLoginService(PasswordStoreFactory.get());
            login.validate(bookmark, LocaleFactory.localizedString("Amazon CloudFront", "S3"), prompt, options);
            return run.call();
        }
        catch(LoginFailureException failure) {
            bookmark.setCredentials(prompt.prompt(bookmark, bookmark.getCredentials().getUsername(),
                LocaleFactory.localizedString("Login failed", "Credentials"), failure.getMessage(), options));
            return this.authenticated(run, prompt);
        }
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
     * @throws BackgroundException
     */
    @Override
    public void restore(final Path file, final LoginCallback prompt) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        try {
            this.authenticated(new Authenticated<Void>() {
                @Override
                public Void call() throws BackgroundException {
                    try {
                        final AmazonS3 client = client(container);
                        // Standard - S3 Standard retrievals allow you to access any of your archived objects within several hours.
                        // This is the default option for the GLACIER and DEEP_ARCHIVE retrieval requests that do not specify
                        // the retrieval option. S3 Standard retrievals typically complete within 3-5 hours from the GLACIER
                        // storage class and typically complete within 12 hours from the DEEP_ARCHIVE storage class.
                        client.restoreObjectV2(new RestoreObjectRequest(container.getName(), containerService.getKey(file))
                            // To restore a specific object version, you can provide a version ID. If you don't provide a version ID, Amazon S3 restores the current version.
                            .withVersionId(file.attributes().getVersionId())
                            .withExpirationInDays(preferences.getInteger("s3.glacier.restore.expiration.days"))
                            .withGlacierJobParameters(new GlacierJobParameters().withTier(preferences.getProperty("s3.glacier.restore.tier")))
                        );
                        // 200 Reply if already restored
                    }
                    catch(AmazonClientException e) {
                        throw new AmazonServiceExceptionMappingService().map("Failure to write attributes of {0}", e, file);
                    }
                    return null;
                }
            }, prompt);
        }
        catch(ConflictException e) {
            // 409 when restore is in progress
            log.warn(String.format("Restore for %s already in progress %s", file, e));
        }
    }

    @Override
    public boolean isRestorable(final Path file) {
        return StringUtils.equals("GLACIER", file.attributes().getStorageClass()) ||
            StringUtils.equals("DEEP_ARCHIVE", file.attributes().getStorageClass());
    }

    private AmazonS3 client(final Path container) throws BackgroundException {
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withCredentials(AWSCredentialsConfigurator.toAWSCredentialsProvider(bookmark.getCredentials()))
            .withClientConfiguration(configuration);
        final Location.Name region = this.getRegion(container);
        if(Location.unknown.equals(region)) {
            builder.withRegion(Regions.DEFAULT_REGION);
        }
        else {
            builder.withRegion(region.getIdentifier());
        }
        return builder.build();
    }

    protected Location.Name getRegion(final Path container) throws BackgroundException {
        return locationFeature.getLocation(container);
    }
}
