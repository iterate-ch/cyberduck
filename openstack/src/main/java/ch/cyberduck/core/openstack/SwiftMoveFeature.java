package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Collections;
import java.util.Optional;

public class SwiftMoveFeature implements Move {

    private final PathContainerService containerService
            = new DefaultPathContainerService();

    private final SwiftSession session;
    private final SwiftRegionService regionService;
    private final SwiftDeleteFeature delete;
    private final SwiftDefaultCopyFeature proxy;

    public SwiftMoveFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftMoveFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
        this.delete = new SwiftDeleteFeature(session);
        this.proxy = new SwiftDefaultCopyFeature(session, regionService);
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        if(new DefaultPathPredicate(containerService.getContainer(file)).test(containerService.getContainer(renamed))) {
            // Either copy complete file contents (small file) or copy manifest (large file)
            final Path rename = proxy.copy(file, renamed, new TransferStatus().withLength(file.attributes().getSize()), connectionCallback, new DisabledStreamListener());
            delete.delete(Collections.singletonMap(file, status), connectionCallback, callback, false);
            return rename;
        }
        else {
            final Path copy = new SwiftSegmentCopyService(session, regionService).copy(file, renamed, new TransferStatus().withLength(file.attributes().getSize()), connectionCallback, new DisabledStreamListener());
            delete.delete(Collections.singletonMap(file, status), connectionCallback, callback);
            return copy;
        }
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        proxy.preflight(source, target);
        delete.preflight(source);
    }
}
