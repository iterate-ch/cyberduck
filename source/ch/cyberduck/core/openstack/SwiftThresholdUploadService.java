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

/**
 * @author Joel Wright <joel.wright@sohonet.com>
 * @version $Id$
 */
public class SwiftThresholdUploadService implements Upload {
    private static final Logger log = Logger.getLogger(SwiftThresholdUploadService.class);

    private SwiftSession session;

    private Long threshold;

    /**
     * Segment size
     */
    private Long segment;

    public SwiftThresholdUploadService(final SwiftSession session) {
        this(session, PreferencesFactory.get().getLong("openstack.upload.largeobject.threshold"),
                PreferencesFactory.get().getLong("openstack.upload.largeobject.size"));
    }

    public SwiftThresholdUploadService(final SwiftSession session, final Long threshold, final Long segment) {
        this.session = session;
        this.threshold = threshold;
        this.segment = segment;
    }

    @Override
    public boolean pooled() {
        return true;
    }

    @Override
    public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        return session.getFeature(Write.class).append(file, length, cache);
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
                    final SwiftSmallObjectUploadFeature single = new SwiftSmallObjectUploadFeature(session);
                    return single.upload(file, local, throttle, listener, status, callback);
                }
            }
            feature = new SwiftLargeObjectUploadFeature(session, segment);
        }
        else {
            feature = new SwiftSmallObjectUploadFeature(session);
        }
        // Previous segments to delete
        final List<Path> segments = new ArrayList<Path>();
        if(PreferencesFactory.get().getBoolean("openstack.upload.largeobject.cleanup")) {
            if(!status.isAppend()) {
                // Cleanup if necessary
                segments.addAll(new SwiftSegmentService(session).list(file));
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
