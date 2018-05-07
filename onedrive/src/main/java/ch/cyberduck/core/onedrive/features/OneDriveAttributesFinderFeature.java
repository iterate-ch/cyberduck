package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.onedrive.OneDriveExceptionMappingService;
import ch.cyberduck.core.onedrive.OneDriveSession;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDriveRemoteItem;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OneDriveAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(OneDriveAttributesFinderFeature.class);

    private final OneDriveSession session;

    private final PathContainerService containerService
        = new PathContainerService();

    public OneDriveAttributesFinderFeature(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }

        final OneDriveItem item = session.toItem(file);
        try {
            final OneDriveItem.Metadata metadata = item.getMetadata();
            return this.convert(metadata);
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    public PathAttributes convert(final OneDriveItem.Metadata metadata) {
        final PathAttributes attributes = new PathAttributes();
        annotate(attributes, metadata);
        return attributes;
    }

    public void annotate(final PathAttributes attributes, final OneDriveItem.Metadata metadata) {
        attributes.setETag(metadata.getETag());
        attributes.setSize(metadata.getSize());

        if(metadata instanceof OneDriveRemoteItem.Metadata) {
            final OneDriveRemoteItem.Metadata remoteMetadata = (OneDriveRemoteItem.Metadata) metadata;
            final OneDriveItem.Metadata originMetadata = remoteMetadata.getRemoteItem();

            attributes.setVersionId(String.join("/",
                metadata.getParentReference().getDriveId(), metadata.getId(),
                originMetadata.getParentReference().getDriveId(), originMetadata.getId()));
        }
        else {
            attributes.setVersionId(String.join("/", metadata.getParentReference().getDriveId(), metadata.getId()));
        }

        try {
            attributes.setLink(new DescriptiveUrl(new URI(metadata.getWebUrl()), DescriptiveUrl.Type.http));
        }
        catch(URISyntaxException e) {
            log.warn(String.format("Cannot set link. Web URL returned %s", metadata.getWebUrl()), e);
        }
        if(null != metadata.getFileSystemInfo()) {
            attributes.setModificationDate(metadata.getFileSystemInfo().getLastModifiedDateTime().toInstant().toEpochMilli());
            attributes.setCreationDate(metadata.getFileSystemInfo().getCreatedDateTime().toInstant().toEpochMilli());
        }
        else {
            attributes.setModificationDate(metadata.getLastModifiedDateTime().toInstant().toEpochMilli());
            attributes.setCreationDate(metadata.getCreatedDateTime().toInstant().toEpochMilli());
        }
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        return this;
    }
}
