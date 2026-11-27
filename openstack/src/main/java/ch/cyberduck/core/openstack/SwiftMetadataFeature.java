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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.shared.MetadataSizeFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

import ch.iterate.openstack.swift.Constants;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerMetadata;

public class SwiftMetadataFeature implements Metadata {
    private static final Logger log = LogManager.getLogger(SwiftMetadataFeature.class);

    /**
     * Maximum number of bytes of metadata name with default cluster constraints
     */
    public static final int MAXIMUM_NAME_SIZE = 128;
    /**
     * Maximum number of bytes of metadata value with default cluster constraints
     */
    public static final int MAXIMUM_VALUE_SIZE = 256;
    /**
     * Maximum number of metadata entries with default cluster constraints
     */
    public static final int MAXIMUM_COUNT = 90;
    /**
     * Maximum number of bytes of all metadata names and values with default cluster constraints
     */
    public static final int MAXIMUM_TOTAL_SIZE = 4096;

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
    public Map<String, String> getDefault(final Path file) {
        final Map<String, String> metadata = HostPreferencesFactory.get(session.getHost()).getMap("openstack.metadata.default");
        if(metadata.isEmpty()) {
            return Metadata.EMPTY;
        }
        return metadata;
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
                    containerService.getContainer(file).getName(), filter(file, status.getMetadata()));
            }
            else {
                log.debug("Write metadata {} for file {}", status, file);
                session.getClient().updateObjectMetadata(regionService.lookup(file),
                    containerService.getContainer(file).getName(), containerService.getKey(file), filter(file, status.getMetadata()));
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }

    /**
     * @param file     File
     * @param metadata Metadata to write
     * @return Metadata with entries exceeding limits removed. Editable HTTP headers are not subject to limits
     */
    public static Map<String, String> filter(final Path file, final Map<String, String> metadata) {
        return new MetadataSizeFilter()
                .withMaximumKeySize(MAXIMUM_NAME_SIZE)
                .withMaximumValueSize(MAXIMUM_VALUE_SIZE)
                .withMaximumCount(MAXIMUM_COUNT)
                .withMaximumTotalSize(MAXIMUM_TOTAL_SIZE)
                .withExcluded(key -> Constants.HTTP_HEADER_EDITABLE_NAMES.contains(StringUtils.lowerCase(key)))
                // Values are percent encoded in headers
                .withEncoder(value -> {
                    try {
                        return new URLCodec().encode(value).replaceAll("\\+", "%20");
                    }
                    catch(EncoderException e) {
                        return value;
                    }
                })
                .filter(file, metadata);
    }
}
