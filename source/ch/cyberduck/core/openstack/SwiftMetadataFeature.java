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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.features.Headers;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.rackspacecloud.client.cloudfiles.FilesContainerMetaData;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;

/**
 * @version $Id$
 */
public class SwiftMetadataFeature implements Headers {
    private static final Logger log = Logger.getLogger(SwiftMetadataFeature.class);

    private SwiftSession session;

    private PathContainerService containerService = new PathContainerService();

    public SwiftMetadataFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            if(file.attributes().isFile()) {
                final FilesObjectMetaData meta
                        = session.getClient().getObjectMetaData(session.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
                return meta.getMetaData();
            }
            else if(containerService.isContainer(file)) {
                final FilesContainerMetaData meta
                        = session.getClient().getContainerMetaData(session.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName());
                return meta.getMetaData();
            }
            return Collections.emptyMap();
        }
        catch(FilesException e) {
            throw new SwiftExceptionMappingService().map("Cannot read file attributes", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, file);
        }
    }


    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        try {
            if(file.attributes().isFile()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Write metadata %s for file %s", metadata, file));
                }
                session.getClient().updateObjectMetadata(session.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file), metadata);
            }
            else if(containerService.isContainer(file)) {
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
                session.getClient().updateContainerMetadata(session.getRegion(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), metadata);
            }
        }
        catch(FilesException e) {
            throw new SwiftExceptionMappingService().map("Cannot write file attributes", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot write file attributes", e, file);
        }
    }
}
