package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.TransferAcceleration;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class S3BulkTransferAccelerationFeature implements Bulk<Void> {
    private static final Logger log = Logger.getLogger(S3BulkTransferAccelerationFeature.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private final S3Session session;
    private final TransferAcceleration accelerationService;

    public S3BulkTransferAccelerationFeature(final S3Session session) {
        this(session, session.getFeature(TransferAcceleration.class));
    }

    public S3BulkTransferAccelerationFeature(final S3Session session, final TransferAcceleration accelerationService) {
        this.session = session;
        this.accelerationService = accelerationService;
    }

    @Override
    public Void pre(final Transfer.Type type, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        this.configure(files, callback, true);
        return null;
    }

    @Override
    public void post(final Transfer.Type type, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        this.configure(files, callback, false);
    }

    @Override
    public Bulk<Void> withDelete(final Delete delete) {
        return this;
    }

    private void configure(final Map<Path, TransferStatus> files, final ConnectionCallback callback, final boolean enabled) throws BackgroundException {
        final Set<Path> buckets = new HashSet<>();
        for(Path file : files.keySet()) {
            buckets.add(new S3PathContainerService().getContainer(file));
        }
        for(Path bucket : buckets) {
            try {
                if(this.accelerate(bucket, callback)) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Tunnel upload for file %s through accelerated endpoint %s", bucket, accelerationService));
                    }
                    accelerationService.configure(enabled, bucket);
                }
                else {
                    log.warn(String.format("Transfer acceleration disabled for %s", bucket));
                }
            }
            catch(AccessDeniedException e) {
                log.warn(String.format("Ignore failure reading S3 accelerate configuration. %s", e.getMessage()));
            }
        }
    }

    private boolean accelerate(final Path file, final ConnectionCallback prompt) throws BackgroundException {
        switch(session.getSignatureVersion()) {
            case AWS2:
                return false;
        }
        if(accelerationService.getStatus(file)) {
            log.info(String.format("S3 transfer acceleration enabled for file %s", file));
            return true;
        }
        if(preferences.getBoolean("s3.accelerate.prompt")) {
            if(accelerationService.prompt(session.getHost(), file, prompt)) {
                log.info(String.format("S3 transfer acceleration enabled for file %s", file));
                return true;
            }
        }
        return false;
    }
}