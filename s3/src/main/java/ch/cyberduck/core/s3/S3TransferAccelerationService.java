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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.TransferAcceleration;

import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.AccelerateConfig;
import org.jets3t.service.utils.ServiceUtils;

public class S3TransferAccelerationService implements TransferAcceleration {

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
    public boolean getStatus(final Path file) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        try {
            return session.getClient().getAccelerateConfig(bucket.getName()).isEnabled();
        }
        catch(S3ServiceException failure) {
            throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", failure, bucket);
        }
    }

    @Override
    public void setStatus(final Path file, final boolean enabled) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        try {
            if(!ServiceUtils.isBucketNameValidDNSName(bucket.getName())) {
                throw new InteroperabilityException("The name of the bucket used for Transfer Acceleration must be DNS-compliant and must not contain periods.");
            }
            session.getClient().setAccelerateConfig(bucket.getName(), new AccelerateConfig(enabled));
        }
        catch(S3ServiceException failure) {
            throw new S3ExceptionMappingService().map("Failure to write attributes of {0}", failure, bucket);
        }
    }

    @Override
    public boolean prompt(final Host bookmark, final Path file, final ConnectionCallback prompt) throws BackgroundException {
        try {
            // Read transfer acceleration state. Enabled | Suspended
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
            this.setStatus(file, true);
            return true;
        }
    }

    @Override
    public void configure(final boolean enable, final Path file) throws BackgroundException {
        final Jets3tProperties options = session.getClient().getJetS3tProperties();
        if(enable) {
            // Set accelerated endpoint
            options.setProperty("s3service.s3-endpoint", hostname);
            options.setProperty("s3service.disable-dns-buckets", String.valueOf(false));
            options.setProperty("s3service.disable-expect-continue", String.valueOf(true));
        }
        else {
            // Revert default configuration
            options.loadAndReplaceProperties(session.configure(), this.toString());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("S3TransferAccelerationService{");
        sb.append("hostname='").append(hostname).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
