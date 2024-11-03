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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerMetadata;

public class SwiftMetadataFeature implements Headers {
    private static final Logger log = LogManager.getLogger(SwiftMetadataFeature.class);

    private final SwiftSession session;
    private final PathContainerService containerService = new DefaultPathContainerService();
    private final SwiftRegionService regionService;

    public SwiftMetadataFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftMetadataFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public Map<String, String> getDefault(final Local local) {
        return new HostPreferences(session.getHost()).getMap("openstack.metadata.default");
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
                return new SwiftAttributesFinderFeature(session).find(file).getMetadata();
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
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                for(Map.Entry<String, String> entry : file.attributes().getMetadata().entrySet()) {
                    // Choose metadata values to remove
                    if(!status.getMetadata().containsKey(entry.getKey())) {
                        log.debug("Remove metadata with key {}", entry.getKey());
                        status.getMetadata().put(entry.getKey(), StringUtils.EMPTY);
                    }
                }
                log.debug("Write metadata {} for file {}", status, file);
                session.getClient().updateContainerMetadata(regionService.lookup(file),
                    containerService.getContainer(file).getName(), status.getMetadata());
            }
            else {
                log.debug("Write metadata {} for file {}", status, file);
                session.getClient().updateObjectMetadata(regionService.lookup(file),
                    containerService.getContainer(file).getName(), containerService.getKey(file), status.getMetadata());
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
