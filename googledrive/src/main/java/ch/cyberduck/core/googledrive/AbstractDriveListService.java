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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.webloc.UrlFileWriter;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public abstract class AbstractDriveListService implements ListService {
    private static final Logger log = Logger.getLogger(AbstractDriveListService.class);

    protected static final String GOOGLE_APPS_PREFIX = "application/vnd.google-apps";
    protected static final String DRIVE_FOLDER = String.format("%s.folder", GOOGLE_APPS_PREFIX);
    protected static final String DEFAULT_FIELDS = "files(createdTime,explicitlyTrashed,id,md5Checksum,mimeType,modifiedTime,name,size,webViewLink),nextPageToken";

    private final DriveSession session;
    private final int pagesize;
    private final UrlFileWriter urlFileWriter = UrlFileWriterFactory.get();
    private final String fields;
    private final DriveAttributesFinderFeature attributes;
    private final DriveFileidProvider fileid;

    public AbstractDriveListService(final DriveSession session, final DriveFileidProvider fileid) {
        this(session, fileid, PreferencesFactory.get().getInteger("googledrive.list.limit"));
    }

    public AbstractDriveListService(final DriveSession session, final DriveFileidProvider fileid, final int pagesize) {
        this(session, fileid, pagesize, DEFAULT_FIELDS);
    }

    public AbstractDriveListService(final DriveSession session, final DriveFileidProvider fileid, final int pagesize, final String fields) {
        this.session = session;
        this.fileid = fileid;
        this.pagesize = pagesize;
        this.fields = fields;
        this.attributes = new DriveAttributesFinderFeature(session, fileid);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            String page = null;
            final String query = this.query(directory, listener);
            do {
                final FileList list = session.getClient().files().list()
                    // Whether Team Drive items should be included in results
                    .setIncludeTeamDriveItems(true)
                    // Whether the requesting application supports Team Drives
                    .setSupportsTeamDrives(PreferencesFactory.get().getBoolean("googledrive.teamdrive.enable"))
                    .setQ(query)
                    .setOrderBy("name")
                    .setPageToken(page)
                    .setFields(fields)
                    .setPageSize(pagesize).execute();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Chunk of %d retrieved", list.getFiles().size()));
                }
                for(File f : list.getFiles()) {
                    final PathAttributes properties = attributes.toAttributes(f);
                    final String filename;
                    if(!DRIVE_FOLDER.equals(f.getMimeType()) && StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)) {
                        filename = String.format("%s.%s", PathNormalizer.name(f.getName()), urlFileWriter.getExtension());
                    }
                    else {
                        filename = PathNormalizer.name(f.getName());
                    }
                    if(StringUtils.equals(filename, String.valueOf(Path.DELIMITER))) {
                        continue;
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

    protected abstract String query(final Path directory, final ListProgressListener listener) throws BackgroundException;

    @Override
    public ListService withCache(final Cache<Path> cache) {
        fileid.withCache(cache);
        return this;
    }
}
