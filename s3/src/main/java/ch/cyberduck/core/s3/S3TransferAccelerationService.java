package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.accelerate.TransferAccelerationService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RegionEndpointCache;
import org.jets3t.service.model.AccelerateConfig;
import org.jets3t.service.utils.ServiceUtils;

public class S3TransferAccelerationService implements TransferAccelerationService<S3Session> {

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3Session session;

    public static final String S3_ACCELERATE_HOSTNAME = "s3-accelerate.amazonaws.com";
    public static final String S3_ACCELERATE_DUALSTACK_HOSTNAME = "s3-accelerate.dualstack.amazonaws.com";

    private final String hostname;

    public S3TransferAccelerationService(final S3Session session) {
        this(session, S3_ACCELERATE_DUALSTACK_HOSTNAME);
    }

    public S3TransferAccelerationService(final S3Session session, final String hostname) {
        this.session = session;
        this.hostname = hostname;
    }

    @Override
    public boolean prompt(final Host bookmark, final Path file, final TransferStatus status,
                          final ConnectionCallback prompt) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        if(!ServiceUtils.isBucketNameValidDNSName(bucket.getName())) {
            // The name of the bucket used for Transfer Acceleration must be DNS-compliant and must not contain periods (".").
            return false;
        }
        try {
            // Read transfer acceleration state. Enabled | Suspended
            final boolean enabled;
            try {
                enabled = session.getClient().getAccelerateConfig(bucket.getName()).isEnabled();
            }
            catch(S3ServiceException failure) {
                throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", failure, bucket);
            }
            if(enabled) {
                return true;
            }
            prompt.warn(bookmark.getProtocol(), LocaleFactory.localizedString("Enable Amazon S3 Transfer Acceleration", "S3"),
                    LocaleFactory.localizedString("Amazon S3 Transfer Acceleration makes data transfers into and out of Amazon S3 buckets faster, and only charges if there is a performance improvement.", "S3"),
                    LocaleFactory.localizedString("Continue", "Credentials"),
                    LocaleFactory.localizedString("Change", "Credentials"),
                    String.format("s3.acceleration.%s", bookmark.getHostname())
            );
            // Continue chosen
            return false;
        }
        catch(ConnectionCanceledException e) {
            // Enable transfer acceleration for bucket
            try {
                session.getClient().setAccelerateConfig(bucket.getName(), new AccelerateConfig(true));
            }
            catch(S3ServiceException failure) {
                throw new S3ExceptionMappingService().map("Failure to write attributes of {0}", failure, bucket);
            }
            return true;
        }
    }

    @Override
    public S3Session open(final Host bookmark, final Path file, final X509TrustManager trust,
                          final X509KeyManager key) throws BackgroundException {
        final S3Session proxy = new S3Session(new Host(bookmark.getProtocol(), hostname, bookmark.getCredentials()), trust, key) {
            @Override
            protected Jets3tProperties configure() {
                final Jets3tProperties options = super.configure();
                options.setProperty("s3service.disable-dns-buckets", String.valueOf(false));
                options.setProperty("s3service.disable-expect-continue", String.valueOf(true));
                return options;
            }
        };
        final RequestEntityRestStorageService client = proxy.open(new DisabledHostKeyCallback(), session);
        // Swap credentials. No login required
        client.setProviderCredentials(session.getClient().getProviderCredentials());
        final RegionEndpointCache cache = session.getClient().getRegionEndpointCache();
        cache.putRegionForBucketName(containerService.getContainer(file).getName(), session.getFeature(Location.class).getLocation(file).getIdentifier());
        client.setRegionEndpointCache(cache);
        return proxy;
    }
}
