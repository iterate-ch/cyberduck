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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.DriveItemVersion;
import org.nuxeo.onedrive.client.types.FileSystemInfo;
import org.nuxeo.onedrive.client.types.Publication;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Optional;


public class GraphAttributesFinderFeature implements AttributesFinder, AttributesAdapter<DriveItem.Metadata> {
    private static final Logger log = LogManager.getLogger(GraphAttributesFinderFeature.class);

    private final GraphSession session;
    private final GraphFileIdProvider fileid;

    public GraphAttributesFinderFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
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

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(!session.isAccessible(file)) {
            return PathAttributes.EMPTY;
        }
        final DriveItem item = session.getItem(file);
        try {
            return this.toAttributes(this.toMetadata(file, item));
        }
        catch(NotfoundException e) {
            return this.toAttributes(this.toMetadata(file, session.getItem(file)));
        }
    }

    private DriveItem.Metadata toMetadata(final Path file, final DriveItem item) throws BackgroundException {
        try {
            return session.getMetadata(item, null);
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
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
        setId(attributes, session.getFileId(metadata));
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
        final Publication publication = metadata.getPublication();
        if(null != publication && publication.getLevel() == Publication.State.checkout) {
            attributes.setLockId(publication.getVersionId());
        }
        return attributes;
    }

    public PathAttributes toAttributes(final DriveItem.Metadata metadata, final DriveItemVersion version) {
        final PathAttributes attributes = toAttributes(metadata);
        attributes.setVersionId(version.getId());
        attributes.setDuplicate(true);
        attributes.setSize(version.getSize());
        attributes.setModificationDate(version.getLastModifiedDateTime().toInstant().toEpochMilli());
        return attributes;
    }

    private void setId(final PathAttributes attributes, final String id) {
        attributes.setFileId(id);
    }
}
