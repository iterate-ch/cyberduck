package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.shared.DefaultCopyFeature;
import ch.cyberduck.core.transfer.TransferStatus;

public class SDSDelegatingCopyFeature implements Copy {

    private final SDSNodeIdProvider nodeid;
    private final SDSCopyFeature proxy;
    private final DefaultCopyFeature copy;

    public SDSDelegatingCopyFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final SDSCopyFeature proxy) {
        this.nodeid = nodeid;
        this.proxy = proxy;
        this.copy = new DefaultCopyFeature(session);
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(proxy.isSupported(source, target)) {
            return proxy.copy(source, target, status, callback);
        }
        // Copy between encrypted and unencrypted data room
        if(nodeid.isEncrypted(target)) {
            // File key must be set for new upload
            nodeid.setFileKey(status);
        }
        return copy.copy(source, target, status, callback);
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        if(proxy.isSupported(source, target)) {
            return proxy.isRecursive(source, target);
        }
        return copy.isRecursive(source, target);
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(proxy.isSupported(source, target)) {
            return true;
        }
        return copy.isSupported(source, target);
    }

    @Override
    public Copy withTarget(final Session<?> session) {
        proxy.withTarget(session);
        return this;
    }
}
