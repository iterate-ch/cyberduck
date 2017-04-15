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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.webloc.UrlFileWriter;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.EnumSet;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public abstract class AbstractDriveListService implements ListService {
    private static final Logger log = Logger.getLogger(AbstractDriveListService.class);

    protected static final String GOOGLE_APPS_PREFIX = "application/vnd.google-apps";
    protected static final String DRIVE_FOLDER = String.format("%s.folder", GOOGLE_APPS_PREFIX);

    private final DriveSession session;
    private final int pagesize;
    private final UrlFileWriter urlFileWriter = UrlFileWriterFactory.get();

    public AbstractDriveListService(final DriveSession session) {
        this(session, PreferencesFactory.get().getInteger("googledrive.list.limit"));
    }

    public AbstractDriveListService(final DriveSession session, final int pagesize) {
        this.session = session;
        this.pagesize = pagesize;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            String page = null;
            do {
                final FileList list = session.getClient().files().list()
                        .setQ(this.query(directory))
                        .setPageToken(page)
                        .setFields("files(createdTime,explicitlyTrashed,id,md5Checksum,mimeType,modifiedTime,name,size,webViewLink),nextPageToken")
                        .setPageSize(pagesize).execute();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Chunk of %d retrieved", list.getFiles().size()));
                }
                for(File f : list.getFiles()) {
                    final PathAttributes properties = this.toAttributes(f);
                    if(properties == null) {
                        continue;
                    }
                    final String filename;
                    if(!DRIVE_FOLDER.equals(f.getMimeType()) && StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)) {
                        filename = String.format("%s.%s", PathNormalizer.name(f.getName()), urlFileWriter.getExtension());
                    }
                    else {
                        filename = PathNormalizer.name(f.getName());
                    }
                    // Use placeholder type to mark Google Apps document to download as web link file
                    final EnumSet<AbstractPath.Type> type = DRIVE_FOLDER.equals(f.getMimeType()) ? EnumSet.of(Path.Type.directory) :
                            StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)
                                    ? EnumSet.of(Path.Type.file, Path.Type.placeholder) : EnumSet.of(Path.Type.file);

                    final Path child = new Path(directory, filename, type, properties);
                    children.add(child);
                }
                listener.chunk(directory, children);
                page = list.getNextPageToken();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Continue with next page token %s", page));
                }
            }
            while(page != null);
            return children;
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Listing directory failed", e, directory);
        }
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
            if(!StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)) {
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
            if(StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)) {
                attributes.setSize(UrlFileWriterFactory.get().write(new DescriptiveUrl(URI.create(f.getWebViewLink())))
                        .getBytes(Charset.defaultCharset()).length);
            }
        }
        return attributes;
    }

    protected abstract String query(final Path directory) throws BackgroundException;
}
