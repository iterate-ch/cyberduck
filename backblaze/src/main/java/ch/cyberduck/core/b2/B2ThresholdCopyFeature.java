package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class B2ThresholdCopyFeature implements Copy {
    private static final Logger log = LogManager.getLogger(B2ThresholdCopyFeature.class);

    private final B2Session session;
    private final B2VersionIdProvider fileid;
    private final Long threshold;

    public B2ThresholdCopyFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getLong("b2.copy.largeobject.threshold"));
    }

    public B2ThresholdCopyFeature(final B2Session session, final B2VersionIdProvider fileid, final Long threshold) {
        this.session = session;
        this.fileid = fileid;
        this.threshold = threshold;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        if(new B2ThresholdUploadService(session, fileid, threshold).threshold(status.getLength())) {
            return new B2LargeCopyFeature(session, fileid).copy(source, target, status, callback, listener);
        }
        else {
            return new B2CopyFeature(session, fileid).copy(source, target, status, callback, listener);
        }
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return new B2CopyFeature(session, fileid).isSupported(source, target);
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return new B2CopyFeature(session, fileid).isRecursive(source, target);
    }
}
