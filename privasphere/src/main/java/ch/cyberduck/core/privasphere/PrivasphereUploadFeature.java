package ch.cyberduck.core.privasphere;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

public class PrivasphereUploadFeature implements Upload<Object> {

    private final PrivasphereSession session;

    public PrivasphereUploadFeature(final PrivasphereSession session) {
        this.session = session;
    }

    @Override
    public Object upload(final Path path, final Local local, final BandwidthThrottle bandwidthThrottle, final StreamListener streamListener, final TransferStatus transferStatus, final ConnectionCallback connectionCallback) throws BackgroundException {
        return null;
    }

    @Override
    public Write.Append append(final Path path, final Long aLong, final Cache<Path> cache) throws BackgroundException {
        return null;
    }

    @Override
    public Upload<Object> withWriter(final Write<Object> write) {
        return null;
    }
}
