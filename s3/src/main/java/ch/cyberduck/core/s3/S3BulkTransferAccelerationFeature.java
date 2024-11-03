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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.TransferAcceleration;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class S3BulkTransferAccelerationFeature implements Bulk<Void> {
    private static final Logger log = LogManager.getLogger(S3BulkTransferAccelerationFeature.class);

    private final S3Session session;
    private final TransferAcceleration accelerationService;
    private final PathContainerService containerService;

    public S3BulkTransferAccelerationFeature(final S3Session session) {
        this(session, session.getFeature(TransferAcceleration.class));
    }

    public S3BulkTransferAccelerationFeature(final S3Session session, final TransferAcceleration accelerationService) {
        this.session = session;
        this.accelerationService = accelerationService;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public Void pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        this.configure(files, callback, true);
        return null;
    }

    @Override
    public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        this.configure(files, callback, false);
    }

    @Override
    public Bulk<Void> withDelete(final Delete delete) {
        return this;
    }

    private void configure(final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback, final boolean enabled) throws BackgroundException {
        final Set<Path> buckets = new HashSet<>();
        for(TransferItem file : files.keySet()) {
            final Path bucket = containerService.getContainer(file.remote);
            if(!bucket.isRoot()) {
                buckets.add(bucket);
            }
        }
        for(Path bucket : buckets) {
            if(enabled) {
                try {
                    if(this.accelerate(bucket, callback)) {
                        log.info("Tunnel upload for file {} through accelerated endpoint {}", bucket, accelerationService);
                        accelerationService.configure(true, bucket);
                        break;
                    }
                    else {
                        log.warn("Transfer acceleration disabled for {}", bucket);
                    }
                }
                catch(NotfoundException | InteroperabilityException | AccessDeniedException e) {
                    log.warn("Ignore failure reading S3 accelerate configuration. {}", e.getMessage());
                }
            }
            else {
                accelerationService.configure(false, bucket);
            }
        }
    }

    private boolean accelerate(final Path bucket, final ConnectionCallback prompt) throws BackgroundException {
        switch(session.getSignatureVersion()) {
            case AWS2:
                return false;
        }
        if(accelerationService.getStatus(bucket)) {
            log.info("S3 transfer acceleration enabled for file {}", bucket);
            return true;
        }
        if(new HostPreferences(session.getHost()).getBoolean("s3.accelerate.prompt")) {
            if(accelerationService.prompt(session.getHost(), bucket, prompt)) {
                log.info("S3 transfer acceleration enabled for file {}", bucket);
                return true;
            }
        }
        return false;
    }
}
