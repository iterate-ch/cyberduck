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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SwiftThresholdUploadService implements Upload {
    private static final Logger log = Logger.getLogger(SwiftThresholdUploadService.class);

    private final SwiftSession session;

    private final SwiftRegionService regionService;

    private final Long threshold;

    public final SwiftLargeObjectUploadFeature largeObjectUploadFeature;

    public final SwiftSmallObjectUploadFeature smallObjectUploadFeature;

    public SwiftThresholdUploadService(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftThresholdUploadService(final SwiftSession session, final SwiftRegionService regionService) {
        this(session, regionService, PreferencesFactory.get().getLong("openstack.upload.largeobject.threshold"),
                PreferencesFactory.get().getLong("openstack.upload.largeobject.size"));
    }


    public SwiftThresholdUploadService(final SwiftSession session, final SwiftRegionService regionService,
                                       final Long threshold, final Long segment) {
        this.session = session;
        this.regionService = regionService;
        this.threshold = threshold;
        this.largeObjectUploadFeature = new SwiftLargeObjectUploadFeature(session, regionService, segment,
                PreferencesFactory.get().getInteger("openstack.upload.largeobject.concurrency"));
        this.smallObjectUploadFeature = new SwiftSmallObjectUploadFeature(session, regionService);
    }

    @Override
    public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        return new SwiftWriteFeature(session, regionService).append(file, length, cache);
    }

    @Override
    public Object upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final Upload feature;
        if(status.getLength() > threshold) {
            if(!PreferencesFactory.get().getBoolean("openstack.upload.largeobject")) {
                // Disabled by user
                if(status.getLength() < PreferencesFactory.get().getLong("openstack.upload.largeobject.required.threshold")) {
                    log.warn("Large upload is disabled with property openstack.upload.largeobject");
                    return smallObjectUploadFeature.upload(file, local, throttle, listener, status, callback);
                }
            }
            feature = largeObjectUploadFeature;
        }
        else {
            feature = smallObjectUploadFeature;
        }
        // Previous segments to delete
        final List<Path> segments = new ArrayList<Path>();
        if(PreferencesFactory.get().getBoolean("openstack.upload.largeobject.cleanup")) {
            if(!status.isAppend()) {
                // Cleanup if necessary
                segments.addAll(new SwiftSegmentService(session, regionService).list(file));
            }
        }
        final Object checksum = feature.upload(file, local, throttle, listener, status, callback);
        if(!segments.isEmpty()) {
            // Clean up any old segments
            new SwiftMultipleDeleteFeature(session).delete(segments, new DisabledLoginCallback(), new Delete.Callback() {
                @Override
                public void delete(final Path file) {
                    //
                }
            });
        }
        return checksum;
    }
}
