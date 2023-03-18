package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.CancellingListProgressListener;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.date.ISO8601DateFormatter;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerInfo;
import ch.iterate.openstack.swift.model.ObjectMetadata;
import ch.iterate.openstack.swift.model.Region;
import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftAttributesFinderFeature implements AttributesFinder, AttributesAdapter<StorageObject> {
    private static final Logger log = LogManager.getLogger(SwiftAttributesFinderFeature.class);

    private final SwiftSession session;
    private final PathContainerService containerService = new DefaultPathContainerService();
    private final RFC1123DateFormatter rfc1123DateFormatter = new RFC1123DateFormatter();
    private final ISO8601DateFormatter iso8601DateFormatter = new ISO8601DateFormatter();
    private final SwiftRegionService regionService;

    public SwiftAttributesFinderFeature(SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftAttributesFinderFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        final Region region = regionService.lookup(file);
        try {
            if(containerService.isContainer(file)) {
                final ContainerInfo info = session.getClient().getContainerInfo(region,
                        containerService.getContainer(file).getName());
                final PathAttributes attributes = new PathAttributes();
                attributes.setSize(info.getTotalSize());
                attributes.setRegion(info.getRegion().getRegionId());
                return attributes;
            }
            final ObjectMetadata metadata;
            try {
                try {
                    metadata = session.getClient().getObjectMetaData(region,
                            containerService.getContainer(file).getName(), containerService.getKey(file));
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Failure to read attributes of {0}", e, file);
                }
            }
            catch(NotfoundException e) {
                if(file.isDirectory()) {
                    // Directory placeholder file may be missing. Still return empty attributes when we find children
                    try {
                        new SwiftObjectListService(session).list(file, new CancellingListProgressListener());
                    }
                    catch(ListCanceledException l) {
                        // Found common prefix
                        return PathAttributes.EMPTY;
                    }
                    catch(NotfoundException n) {
                        throw e;
                    }
                    // Common prefix only
                    return PathAttributes.EMPTY;
                }
                // Try to find pending large file upload
                final Write.Append append = new SwiftWriteFeature(session, regionService).append(file, new TransferStatus());
                if(append.append) {
                    return new PathAttributes().withSize(append.size);
                }
                throw e;
            }
            if(file.isDirectory()) {
                if(!StringUtils.equals(SwiftDirectoryFeature.DIRECTORY_MIME_TYPE, metadata.getMimeType())) {
                    throw new NotfoundException(String.format("File %s has set MIME type %s but expected %s",
                            file.getAbsolute(), metadata.getMimeType(), SwiftDirectoryFeature.DIRECTORY_MIME_TYPE));
                }
            }
            if(file.isFile()) {
                if(StringUtils.equals(SwiftDirectoryFeature.DIRECTORY_MIME_TYPE, metadata.getMimeType())) {
                    throw new NotfoundException(String.format("File %s has set MIME type %s",
                            file.getAbsolute(), metadata.getMimeType()));
                }
            }
            return this.toAttributes(metadata);
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public PathAttributes toAttributes(final StorageObject object) {
        final PathAttributes attributes = new PathAttributes();
        if(StringUtils.isNotBlank(object.getMd5sum())) {
            // For manifest files, the ETag in the response for a GET or HEAD on the manifest file is the MD5 sum of
            // the concatenated string of ETags for each of the segments in the manifest.
            attributes.setChecksum(Checksum.parse(object.getMd5sum()));
        }
        attributes.setSize(object.getSize());
        final String lastModified = object.getLastModified();
        if(lastModified != null) {
            try {
                attributes.setModificationDate(iso8601DateFormatter.parse(lastModified).getTime());
            }
            catch(InvalidDateException e) {
                log.warn(String.format("%s is not ISO 8601 format %s", lastModified, e.getMessage()));
                try {
                    attributes.setModificationDate(rfc1123DateFormatter.parse(lastModified).getTime());
                }
                catch(InvalidDateException f) {
                    log.warn(String.format("%s is not RFC 1123 format %s", lastModified, f.getMessage()));
                }
            }
        }
        return attributes;
    }

    public PathAttributes toAttributes(final ObjectMetadata metadata) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(Long.parseLong(metadata.getContentLength()));
        final String lastModified = metadata.getLastModified();
        try {
            attributes.setModificationDate(Double.valueOf(Double.parseDouble(lastModified) * 1000).longValue());
        }
        catch(NumberFormatException e) {
            log.warn(String.format("%s is not in UNIX Epoch time stamp format %s", lastModified, e.getMessage()));
        }
        if(StringUtils.isNotBlank(metadata.getETag())) {
            final String etag = RegExUtils.removePattern(metadata.getETag(), "\"");
            // For manifest files, the ETag in the response for a GET or HEAD on the manifest file is the MD5 sum of
            // the concatenated string of ETags for each of the segments in the manifest.
            attributes.setChecksum(Checksum.parse(etag));
        }
        attributes.setMetadata(metadata.getMetaData());
        return attributes;
    }
}
