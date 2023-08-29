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
import ch.cyberduck.core.features.TransferAcceleration;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.AccelerateConfig;

public class S3TransferAccelerationService implements TransferAcceleration {
    private static final Logger log = LogManager.getLogger(S3TransferAccelerationService.class);

    private final S3Session session;
    private final PathContainerService containerService;

    public static final String S3_ACCELERATE_DUALSTACK_HOSTNAME = "s3-accelerate.dualstack.amazonaws.com";

    public S3TransferAccelerationService(final S3Session session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public boolean getStatus(final Path file) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        try {
            return session.getClient().getAccelerateConfig(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName()).isEnabled();
        }
        catch(S3ServiceException failure) {
            throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", failure, bucket);
        }
    }

    @Override
    public void setStatus(final Path file, final boolean enabled) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        try {
            session.getClient().setAccelerateConfig(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), new AccelerateConfig(enabled));
        }
        catch(S3ServiceException failure) {
            throw new S3ExceptionMappingService().map("Failure to write attributes of {0}", failure, bucket);
        }
    }

    @Override
    public boolean prompt(final Host bookmark, final Path file, final ConnectionCallback prompt) throws BackgroundException {
        try {
            // Read transfer acceleration state. Enabled | Suspended
            prompt.warn(bookmark, LocaleFactory.localizedString("Enable Amazon S3 Transfer Acceleration", "S3"),
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
    public void configure(final boolean enable, final Path file) {
        final Path bucket = containerService.getContainer(file);
        final Host host = session.getHost();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set S3 transfer acceleration to %s", enable));
        }
        // Set accelerated endpoint
        host.setProperty(String.format("s3.transferacceleration.%s.enable", bucket.getName()), String.valueOf(enable));
        if(enable) {
            host.setProperty("s3.bucket.virtualhost.disable", String.valueOf(false));
            host.setProperty("s3.upload.expect-continue", String.valueOf(false));
        }
        else {
            // Revert default configuration
            host.setProperty("s3.bucket.virtualhost.disable", PreferencesFactory.get().getProperty("s3.bucket.virtualhost.disable"));
            host.setProperty("s3.upload.expect-continue", PreferencesFactory.get().getProperty("s3.upload.expect-continue"));
        }
    }
}
