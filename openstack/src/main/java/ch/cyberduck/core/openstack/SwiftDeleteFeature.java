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
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ch.iterate.openstack.swift.exception.GenericException;

public class SwiftDeleteFeature implements Delete {
    private static final Logger log = LogManager.getLogger(SwiftDeleteFeature.class);

    private final SwiftSession session;
    private final PathContainerService containerService = new DefaultPathContainerService();
    private final SwiftSegmentService segmentService;
    private final SwiftRegionService regionService;

    public SwiftDeleteFeature(final SwiftSession session) {
        this(session, new SwiftSegmentService(session), new SwiftRegionService(session));
    }

    public SwiftDeleteFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this(session, new SwiftSegmentService(session, regionService), regionService);
    }

    public SwiftDeleteFeature(final SwiftSession session, final SwiftSegmentService segmentService,
                              final SwiftRegionService regionService) {
        this.segmentService = segmentService;
        this.regionService = regionService;
        this.session = session;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        this.delete(files, prompt, callback, new HostPreferences(session.getHost()).getBoolean("openstack.delete.largeobject.segments"));
    }

    /**
     * @param deleteSegments Delete segment files referenced in manifest for large file objects
     */
    protected void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback,
                          final boolean deleteSegments) throws BackgroundException {
        for(Path file : files.keySet()) {
            callback.delete(file);
            try {
                if(file.isFile()) {
                    // Collect a list of existing segments. Must do this before deleting the manifest file.
                    final List<Path> segments = segmentService.list(file);
                    session.getClient().deleteObject(regionService.lookup(file),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
                    // Clean up any old segments, only if rename.remote-transferstatus has not been
                    // set. This indicates this has been run as a move-operation, which in turn
                    // copies a manifest for a given file as long as it is on the same container.
                    if(deleteSegments) {
                        for(Path segment : segments) {
                            session.getClient().deleteObject(regionService.lookup(segment),
                                containerService.getContainer(segment).getName(), containerService.getKey(segment));
                        }
                    }
                }
                else if(file.isDirectory()) {
                    if(containerService.isContainer(file)) {
                        session.getClient().deleteContainer(regionService.lookup(file),
                            containerService.getContainer(file).getName());
                    }
                    else {
                        try {
                            session.getClient().deleteObject(regionService.lookup(file),
                                containerService.getContainer(file).getName(), containerService.getKey(file));
                        }
                        catch(GenericException e) {
                            if(new SwiftExceptionMappingService().map(e) instanceof NotfoundException) {
                                log.warn("Ignore missing placeholder object {}", file);
                            }
                            else {
                                throw e;
                            }
                        }
                    }
                }
            }
            catch(GenericException e) {
                throw new SwiftExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }
}
