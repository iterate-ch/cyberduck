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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.collections.Partition;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Region;

public class SwiftMultipleDeleteFeature implements Delete {

    private final SwiftSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    private final SwiftSegmentService segmentService;

    private final SwiftRegionService regionService;

    public SwiftMultipleDeleteFeature(final SwiftSession session) {
        this(session, new SwiftSegmentService(session), new SwiftRegionService(session));
    }

    public SwiftMultipleDeleteFeature(final SwiftSession session, final SwiftSegmentService segmentService,
                                      final SwiftRegionService regionService) {
        this.segmentService = segmentService;
        this.regionService = regionService;
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        if(files.size() == 1) {
            new SwiftDeleteFeature(session, regionService).delete(files, prompt, callback);
        }
        else {
            final Map<Path, List<String>> containers = new HashMap<Path, List<String>>();
            for(Path file : files) {
                if(containerService.isContainer(file)) {
                    continue;
                }
                callback.delete(file);
                final Path container = containerService.getContainer(file);
                if(containers.containsKey(container)) {
                    containers.get(container).add(containerService.getKey(file));
                }
                else {
                    final List<String> keys = new ArrayList<String>();
                    keys.add(containerService.getKey(file));
                    // Collect a list of existing segments. Must do this before deleting the manifest file.
                    for(Path segment : segmentService.list(file)) {
                        keys.add(containerService.getKey(segment));
                    }
                    containers.put(container, keys);
                }
            }
            try {
                for(Map.Entry<Path, List<String>> container : containers.entrySet()) {
                    final Region region = regionService.lookup(container.getKey());
                    final List<String> keys = container.getValue();
                    for(List<String> partition : new Partition<String>(keys, PreferencesFactory.get().getInteger("openstack.delete.multiple.partition"))) {
                        session.getClient().deleteObjects(region, container.getKey().getName(), partition);
                    }
                }
            }
            catch(GenericException e) {
                if(new SwiftExceptionMappingService().map(e) instanceof InteroperabilityException) {
                    new SwiftDeleteFeature(session, regionService).delete(files, prompt, callback);
                    return;
                }
                else {
                    throw new SwiftExceptionMappingService().map("Cannot delete {0}", e, files.iterator().next());
                }
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot delete {0}", e, files.iterator().next());
            }
            for(Path file : files) {
                if(containerService.isContainer(file)) {
                    callback.delete(file);
                    // Finally delete bucket itself
                    try {
                        session.getClient().deleteContainer(regionService.lookup(file),
                                containerService.getContainer(file).getName());
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
    }
}
