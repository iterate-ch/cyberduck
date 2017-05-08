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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.util.Collections;

public class SwiftMoveFeature implements Move {
    private static final Logger log = Logger.getLogger(SwiftMoveFeature.class);

    private final PathContainerService containerService
            = new PathContainerService();

    private final SwiftSession session;
    private final SwiftRegionService regionService;

    private Delete delete;

    public SwiftMoveFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftMoveFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
        this.delete = new SwiftDeleteFeature(session);
    }

    @Override
    public void move(final Path source, final Path renamed, boolean exists, final Delete.Callback callback) throws BackgroundException {
        if(source.isFile()) {
            new SwiftCopyFeature(session, regionService).copy(source, renamed, new TransferStatus());
            delete.delete(Collections.singletonList(source), new DisabledLoginCallback(), callback);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !containerService.isContainer(source);
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

}
