package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.nio.charset.Charset;
import java.text.MessageFormat;

import com.google.api.services.drive.model.File;

import static ch.cyberduck.core.googledrive.AbstractDriveListService.DRIVE_FOLDER;
import static ch.cyberduck.core.googledrive.AbstractDriveListService.GOOGLE_APPS_PREFIX;

public class DriveAttributesFinderFeature implements AttributesFinder {

    private final DriveSession session;
    private final DriveFileidProvider fileid;

    public DriveAttributesFinderFeature(final DriveSession session, final DriveFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        if(new PathContainerService().isContainer(file)) {
            return PathAttributes.EMPTY;
        }
        final Path query;
        if(file.getType().contains(Path.Type.placeholder)) {
            query = new Path(file.getParent(), FilenameUtils.removeExtension(file.getName()), file.getType(), file.attributes());
        }
        else {
            query = file;
        }
        final AttributedList<Path> list = new FileidDriveListService(session, fileid, query).list(file.getParent(), new DisabledListProgressListener());
        final Path found = list.find(new DriveFileidProvider.IgnoreTrashedPathPredicate(file));
        if(null == found) {
            throw new NotfoundException(file.getAbsolute());
        }
        return found.attributes();

    }

    protected PathAttributes toAttributes(final File f) {
        final PathAttributes attributes = new PathAttributes();
        if(null != f.getExplicitlyTrashed()) {
            if(f.getExplicitlyTrashed()) {
                // Mark as hidden
                attributes.setDuplicate(true);
            }
        }
        if(null != f.getSize()) {
            if(!DRIVE_FOLDER.equals(f.getMimeType())
                && !StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)) {
                attributes.setSize(f.getSize());
            }
        }
        attributes.setVersionId(f.getId());
        if(f.getModifiedTime() != null) {
            attributes.setModificationDate(f.getModifiedTime().getValue());
        }
        if(f.getCreatedTime() != null) {
            attributes.setCreationDate(f.getCreatedTime().getValue());
        }
        attributes.setChecksum(Checksum.parse(f.getMd5Checksum()));
        if(StringUtils.isNotBlank(f.getWebViewLink())) {
            attributes.setLink(new DescriptiveUrl(URI.create(f.getWebViewLink()),
                DescriptiveUrl.Type.http,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"), "HTTP")));
            if(!DRIVE_FOLDER.equals(f.getMimeType()) && StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)) {
                attributes.setSize(UrlFileWriterFactory.get().write(new DescriptiveUrl(URI.create(f.getWebViewLink())))
                    .getBytes(Charset.defaultCharset()).length);
            }
        }
        return attributes;
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        fileid.withCache(cache);
        return this;
    }
}
