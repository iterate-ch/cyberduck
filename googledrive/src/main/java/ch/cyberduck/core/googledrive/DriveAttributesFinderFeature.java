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
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.drive.model.File;

import static ch.cyberduck.core.googledrive.AbstractDriveListService.*;
import static ch.cyberduck.core.googledrive.DriveFileidProvider.KEY_FILE_ID;

public class DriveAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(DriveAttributesFinderFeature.class);

    protected static final String DEFAULT_FIELDS = "createdTime,explicitlyTrashed,id,md5Checksum,mimeType,modifiedTime,name,size,webViewLink,shortcutDetails,version";

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
        final Path found = list.find(new SimplePathPredicate(file));
        if(null == found) {
            throw new NotfoundException(file.getAbsolute());
        }
        return found.attributes();

    }

    protected PathAttributes toAttributes(final File f) throws IOException {
        if(DRIVE_SHORTCUT.equals(f.getMimeType())) {
            final File.ShortcutDetails shortcutDetails = f.getShortcutDetails();
            try {
                return this.toAttributes(session.getClient().files().get(shortcutDetails.getTargetId()).setFields(DEFAULT_FIELDS).execute());
            }
            catch(IOException e) {
                log.warn(String.format("Failure %s resolving shortcut for %s", e, e));
                return PathAttributes.EMPTY;
            }
        }
        final PathAttributes attributes = new PathAttributes();
        if(null != f.getExplicitlyTrashed()) {
            if(f.getExplicitlyTrashed()) {
                // Mark as hidden
                attributes.setHidden(true);
            }
        }
        if(null != f.getSize()) {
            if(!DRIVE_FOLDER.equals(f.getMimeType()) && !StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)) {
                attributes.setSize(f.getSize());
            }
        }
        if(f.getVersion() != null) {
            attributes.setVersionId(String.valueOf(f.getVersion()));
        }
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
            if(!DRIVE_FOLDER.equals(f.getMimeType()) && !DRIVE_SHORTCUT.equals(f.getMimeType()) && StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)) {
                attributes.setSize(UrlFileWriterFactory.get().write(new DescriptiveUrl(URI.create(f.getWebViewLink())))
                    .getBytes(Charset.defaultCharset()).length);
            }
        }
        final Map<String, String> custom = new HashMap<>();
        custom.put(KEY_FILE_ID, f.getId());
        attributes.setCustom(custom);

        return attributes;
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        fileid.withCache(cache);
        return this;
    }
}
