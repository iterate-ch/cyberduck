package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftThresholdUploadService implements Upload<StorageObject> {
    private static final Logger log = LogManager.getLogger(SwiftThresholdUploadService.class);

    private final SwiftSession session;
    private final SwiftRegionService regionService;
    private final Long threshold;

    private Write<StorageObject> writer;

    public SwiftThresholdUploadService(final SwiftSession session, final SwiftRegionService regionService,
                                       final SwiftWriteFeature writer) {
        this(session, regionService, writer, HostPreferencesFactory.get(session.getHost()).getLong("openstack.upload.largeobject.threshold"));
    }


    public SwiftThresholdUploadService(final SwiftSession session, final SwiftRegionService regionService,
                                       final SwiftWriteFeature writer,
                                       final Long threshold) {
        this.session = session;
        this.regionService = regionService;
        this.writer = writer;
        this.threshold = threshold;
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        if(this.threshold(status)) {
            return new SwiftLargeObjectUploadFeature(session, regionService, writer).append(file, status);
        }
        return new Write.Append(false).withStatus(status);
    }

    @Override
    public StorageObject upload(final Path file, final Local local, final BandwidthThrottle throttle, final ProgressListener progress, final StreamListener streamListener,
                                final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final Upload<StorageObject> feature;
        if(this.threshold(status)) {
            feature = new SwiftLargeObjectUploadFeature(session, regionService, writer);
        }
        else {
            feature = new SwiftSmallObjectUploadFeature(session, writer);
        }
        return feature.upload(file, local, throttle, progress, streamListener, status, callback);
    }

    protected boolean threshold(final TransferStatus status) {
        if(status.getLength() > threshold) {
            if(!HostPreferencesFactory.get(session.getHost()).getBoolean("openstack.upload.largeobject")) {
                // Disabled by user
                if(status.getLength() < HostPreferencesFactory.get(session.getHost()).getLong("openstack.upload.largeobject.required.threshold")) {
                    log.warn("Large upload is disabled with property openstack.upload.largeobject");
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Upload<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}
