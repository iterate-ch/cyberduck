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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import synapticloop.b2.response.BaseB2Response;

public class B2ThresholdUploadService implements Upload<BaseB2Response> {
    private static final Logger log = Logger.getLogger(B2ThresholdUploadService.class);

    private final B2Session session;
    private Write<BaseB2Response> writer;
    private final Long threshold;

    public B2ThresholdUploadService(final B2Session session) {
        this(session, PreferencesFactory.get().getLong("b2.upload.largeobject.threshold"));
    }

    public B2ThresholdUploadService(final B2Session session, final Long threshold) {
        this.session = session;
        this.writer = new B2WriteFeature(session);
        this.threshold = threshold;
    }

    @Override
    public Write.Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        return new B2WriteFeature(session).append(file, length, cache);
    }

    @Override
    public BaseB2Response upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                 final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(this.threshold(status.getLength())) {
            return new B2LargeUploadService(session, writer, PreferencesFactory.get().getLong("b2.upload.largeobject.size"),
                    PreferencesFactory.get().getInteger("b2.upload.largeobject.concurrency")).upload(file, local, throttle, listener, status, callback);
        }
        else {
            return new B2SingleUploadService(writer).upload(file, local, throttle, listener, status, callback);
        }
    }

    @Override
    public Upload<BaseB2Response> withWriter(final Write<BaseB2Response> writer) {
        this.writer = writer;
        return this;
    }

    protected boolean threshold(final Long length) {
        if(length > threshold) {
            if(!PreferencesFactory.get().getBoolean("b2.upload.largeobject")) {
                // Disabled by user
                if(length < PreferencesFactory.get().getLong("b2.upload.largeobject.required.threshold")) {
                    log.warn("Large upload is disabled with property b2.upload.largeobject.required.threshold");
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
