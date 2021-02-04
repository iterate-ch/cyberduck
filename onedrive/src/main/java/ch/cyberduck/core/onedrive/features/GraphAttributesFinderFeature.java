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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.FileSystemInfo;
import org.nuxeo.onedrive.client.types.ItemReference;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ch.cyberduck.core.onedrive.features.GraphFileIdProvider.KEY_ITEM_ID;

public class GraphAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(GraphAttributesFinderFeature.class);

    private final GraphSession session;

    public GraphAttributesFinderFeature(final GraphSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        final DriveItem item = session.toItem(file);
        try {
            final DriveItem.Metadata metadata = item.getMetadata();
            return this.toAttributes(metadata);
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    public PathAttributes toAttributes(final DriveItem.Metadata metadata) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setETag(metadata.getETag());
        Optional<DescriptiveUrl> webUrl = getWebUrl(metadata);
        if(metadata.isPackage()) {
            webUrl.ifPresent(url -> attributes.setSize(UrlFileWriterFactory.get().write(url).getBytes(Charset.defaultCharset()).length));
        }
        else if(null != metadata.getSize()) {
            attributes.setSize(metadata.getSize());
        }
        final ItemReference parent = metadata.getParentReference();
        if(metadata.getRemoteItem() != null) {
            final DriveItem.Metadata remoteMetadata = metadata.getRemoteItem();
            final ItemReference remoteParent = remoteMetadata.getParentReference();
            if(parent == null) {
                setId(attributes, String.join(String.valueOf(Path.DELIMITER),
                    remoteParent.getDriveId(), remoteParent.getId()));
            }
            else {
                setId(attributes, String.join(String.valueOf(Path.DELIMITER),
                    parent.getDriveId(), metadata.getId(),
                    remoteParent.getDriveId(), remoteMetadata.getId()));
            }
        }
        else {
            setId(attributes, String.join(String.valueOf(Path.DELIMITER), parent.getDriveId(), metadata.getId()));
        }
        webUrl.ifPresent(attributes::setLink);
        final FileSystemInfo info = metadata.getFacet(FileSystemInfo.class);
        if(null != info) {
            if(-1L == info.getLastModifiedDateTime().toInstant().toEpochMilli()) {
                attributes.setModificationDate(metadata.getLastModifiedDateTime().toInstant().toEpochMilli());
            }
            else {
                attributes.setModificationDate(info.getLastModifiedDateTime().toInstant().toEpochMilli());
            }
            if(-1 == info.getCreatedDateTime().toInstant().toEpochMilli()) {
                attributes.setCreationDate(metadata.getCreatedDateTime().toInstant().toEpochMilli());
            }
            else {
                attributes.setCreationDate(info.getCreatedDateTime().toInstant().toEpochMilli());
            }
        }
        else {
            attributes.setModificationDate(metadata.getLastModifiedDateTime().toInstant().toEpochMilli());
            attributes.setCreationDate(metadata.getCreatedDateTime().toInstant().toEpochMilli());
        }
        return attributes;
    }

    private void setId(final PathAttributes attributes, final String id) {
        final Map<String, String> custom = new HashMap<>(attributes.getCustom());
        custom.put(KEY_ITEM_ID, id);
        attributes.setCustom(custom);
    }

    static Optional<DescriptiveUrl> getWebUrl(final DriveItem.Metadata metadata) {
        DescriptiveUrl url = null;
        try {
            url = new DescriptiveUrl(new URI(metadata.getWebUrl()), DescriptiveUrl.Type.http);
        }
        catch(URISyntaxException e) {
            log.warn(String.format("Cannot create URI of WebURL: %s", metadata.getWebUrl()), e);
        }
        return Optional.ofNullable(url);
    }
}
