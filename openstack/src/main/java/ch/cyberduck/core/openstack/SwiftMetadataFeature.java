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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerMetadata;
import ch.iterate.openstack.swift.model.ObjectMetadata;

public class SwiftMetadataFeature implements Headers {
    private static final Logger log = Logger.getLogger(SwiftMetadataFeature.class);

    private SwiftSession session;

    private PathContainerService containerService
            = new SwiftPathContainerService();

    private SwiftRegionService regionService;

    public SwiftMetadataFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftMetadataFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public Map<String, String> getDefault() {
        return PreferencesFactory.get().getMap("openstack.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final ContainerMetadata meta
                        = session.getClient().getContainerMetaData(regionService.lookup(file),
                        containerService.getContainer(file).getName());
                return meta.getMetaData();
            }
            else {
                final ObjectMetadata meta
                        = session.getClient().getObjectMetaData(regionService.lookup(file),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
                return meta.getMetaData();
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }


    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                for(Map.Entry<String, String> entry : file.attributes().getMetadata().entrySet()) {
                    // Choose metadata values to remove
                    if(!metadata.containsKey(entry.getKey())) {
                        log.debug(String.format("Remove metadata with key %s", entry.getKey()));
                        metadata.put(entry.getKey(), StringUtils.EMPTY);
                    }
                }
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Write metadata %s for file %s", metadata, file));
                }
                session.getClient().updateContainerMetadata(regionService.lookup(file),
                        containerService.getContainer(file).getName(), metadata);
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Write metadata %s for file %s", metadata, file));
                }
                session.getClient().updateObjectMetadata(regionService.lookup(file),
                        containerService.getContainer(file).getName(), containerService.getKey(file), metadata);
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
