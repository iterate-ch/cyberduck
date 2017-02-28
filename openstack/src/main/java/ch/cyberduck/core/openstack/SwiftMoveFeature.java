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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import ch.iterate.openstack.swift.exception.GenericException;

public class SwiftMoveFeature implements Move {
    private static final Logger log = Logger.getLogger(SwiftMoveFeature.class);

    private final PathContainerService containerService
            = new PathContainerService();

    private final SwiftSession session;
    private final SwiftRegionService regionService;

    private Delete delete;
    private ListService list;

    public SwiftMoveFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftMoveFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
        this.delete = new SwiftDeleteFeature(session);
        this.list = new SwiftObjectListService(session);
    }

    @Override
    public void move(final Path source, final Path renamed, boolean exists, final Delete.Callback callback) throws BackgroundException {
        try {
            if(source.isFile()) {
                new SwiftCopyFeature(session, regionService).copy(source, renamed);
                session.getClient().deleteObject(regionService.lookup(source),
                        containerService.getContainer(source).getName(), containerService.getKey(source));
            }
            else if(source.isDirectory()) {
                for(Path i : list.list(source, new DisabledListProgressListener())) {
                    this.move(i, new Path(renamed, i.getName(), i.getType()), false, callback);
                }
                try {
                    delete.delete(Collections.singletonList(source), new DisabledLoginCallback(), new Delete.DisabledCallback());
                }
                catch(NotfoundException e) {
                    // No real placeholder but just a delimiter returned in the object listing.
                    log.warn(e.getMessage());
                }
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot rename {0}", e, source);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, source);
        }
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

    @Override
    public Move withList(final ListService list) {
        this.list = list;
        return this;
    }
}
