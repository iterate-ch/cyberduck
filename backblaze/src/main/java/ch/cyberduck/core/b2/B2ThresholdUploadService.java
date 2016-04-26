package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

public class B2ThresholdUploadService implements Upload {
    private static final Logger log = Logger.getLogger(B2ThresholdUploadService.class);

    private final B2Session session;

    private final Long threshold;

    public final B2LargeUploadService largeUploadService;

    public final B2SingleUploadService singleUploadService;

    public B2ThresholdUploadService(final B2Session session) {
        this(session, PreferencesFactory.get().getLong("b2.upload.largeobject.threshold"),
                PreferencesFactory.get().getLong("b2.upload.largeobject.size"));
    }


    public B2ThresholdUploadService(final B2Session session, final Long threshold, final Long partsize) {
        this.session = session;
        this.threshold = threshold;
        this.largeUploadService = new B2LargeUploadService(session, partsize);
        this.singleUploadService = new B2SingleUploadService(session);
    }

    @Override
    public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        if(this.threshold(file, length)) {
            return new B2PartWriteFeature(session).append(file, length, cache);
        }
        // No append
        return new B2WriteFeature(session).append(file, length, cache);
    }

    @Override
    public Object upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final Upload feature;
        if(this.threshold(file, status.getLength())) {
            return largeUploadService.upload(file, local, throttle, listener, status, callback);
        }
        else {
            return singleUploadService.upload(file, local, throttle, listener, status, callback);
        }
    }

    private boolean threshold(final Path file, final Long length) {
        if(length > threshold) {
            if(!PreferencesFactory.get().getBoolean("b2.upload.largeobject")) {
                // Disabled by user
                if(length < PreferencesFactory.get().getLong("b2.upload.largeobject.required.threshold")) {
                    log.warn("Large upload is disabled with property openstack.upload.largeobject");
                    return false;
                }
            }
            return true;
        }
        else {
            // Below threshold
            return false;
        }
    }
}
