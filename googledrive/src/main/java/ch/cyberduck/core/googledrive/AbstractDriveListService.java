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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.webloc.UrlFileWriter;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public abstract class AbstractDriveListService implements ListService {
    private static final Logger log = LogManager.getLogger(AbstractDriveListService.class);

    protected static final String GOOGLE_APPS_PREFIX = "application/vnd.google-apps";
    protected static final String DRIVE_FOLDER = String.format("%s.folder", GOOGLE_APPS_PREFIX);
    protected static final String DRIVE_SHORTCUT = String.format("%s.shortcut", GOOGLE_APPS_PREFIX);
    protected static final String DEFAULT_FIELDS = String.format("files(%s),nextPageToken", DriveAttributesFinderFeature.DEFAULT_FIELDS);

    private final DriveSession session;
    private final DriveFileIdProvider fileid;
    private final int pagesize;
    private final UrlFileWriter urlFileWriter = UrlFileWriterFactory.get();
    private final String fields;
    private final DriveAttributesFinderFeature attributes;

    public AbstractDriveListService(final DriveSession session, final DriveFileIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getInteger("googledrive.list.limit"));
    }

    public AbstractDriveListService(final DriveSession session, final DriveFileIdProvider fileid, final int pagesize) {
        this(session, fileid, pagesize, DEFAULT_FIELDS);
    }

    public AbstractDriveListService(final DriveSession session, final DriveFileIdProvider fileid, final int pagesize, final String fields) {
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
                    .setIncludeItemsFromAllDrives(true)
                    // Whether the requesting application supports Shared Drives
                    .setSupportsAllDrives(new HostPreferences(session.getHost()).getBoolean("googledrive.teamdrive.enable"))
                    .setQ(query)
                    // Please note that there is a current limitation for users with approximately one million files in which the requested sort order is ignored
                    .setOrderBy("name")
                    .setPageToken(page)
                    .setFields(fields)
                    .setPageSize(pagesize).execute();
                log.debug("Chunk of {} retrieved", list.getFiles().size());
                for(File f : list.getFiles()) {
                    final PathAttributes properties = attributes.toAttributes(f);
                    if(PathAttributes.EMPTY == properties) {
                        log.warn("Ignore file {} with unknown attributes", f);
                        continue;
                    }
                    final String filename;
                    if(!DRIVE_FOLDER.equals(f.getMimeType()) && !DRIVE_SHORTCUT.equals(f.getMimeType()) && StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)) {
                        filename = String.format("%s.%s", f.getName(), urlFileWriter.getExtension());
                    }
                    else {
                        filename = f.getName();
                    }
                    if(StringUtils.equals(filename, String.valueOf(Path.DELIMITER))) {
                        continue;
                    }
                    // Use placeholder type to mark Google Apps document to download as web link file
                    final EnumSet<Path.Type> type = this.toType(f);
                    for(Path parent : this.parents(directory, f)) {
                        children.add(new Path(parent, filename, type, properties));
                    }
                }
                // Mark duplicates
                children.toStream().forEach(f -> f.attributes().setDuplicate(children.findAll(new SimplePathPredicate(f) {
                    @Override
                    public boolean test(final Path f) {
                        // Exclude trashed
                        return super.test(f) && !f.attributes().isHidden();
                    }
                }).size() > 1));
                listener.chunk(directory, children);
                page = list.getNextPageToken();
                log.debug("Continue with next page token {}", page);
            }
            while(page != null);
            return children;
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Listing directory failed", e, directory);
        }
    }

    protected Set<Path> parents(final Path directory, final File f) throws BackgroundException {
        return Collections.singleton(directory);
    }

    protected EnumSet<Path.Type> toType(final File f) {
        final EnumSet<Path.Type> type;
        if(DRIVE_SHORTCUT.equals(f.getMimeType())) {
            // Shortcut file details. Only populated for shortcut files, which have the mimeType field set to application/vnd.google-apps.shortcut
            final File.ShortcutDetails shortcutDetails = f.getShortcutDetails();
            type = DRIVE_FOLDER.equals(shortcutDetails.getTargetMimeType()) ? EnumSet.of(Path.Type.directory) :
                StringUtils.startsWith(shortcutDetails.getTargetMimeType(), GOOGLE_APPS_PREFIX)
                    ? EnumSet.of(Path.Type.file, Path.Type.placeholder) : EnumSet.of(Path.Type.file);
        }
        else {
            type = DRIVE_FOLDER.equals(f.getMimeType()) ? EnumSet.of(Path.Type.directory) :
                StringUtils.startsWith(f.getMimeType(), GOOGLE_APPS_PREFIX)
                    ? EnumSet.of(Path.Type.file, Path.Type.placeholder) : EnumSet.of(Path.Type.file);
        }
        return type;
    }

    protected abstract String query(final Path directory, final ListProgressListener listener) throws BackgroundException;

}
