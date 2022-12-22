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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.ui.comparator.TimestampComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;

import com.google.api.services.drive.model.Revision;
import com.google.api.services.drive.model.RevisionList;

public class DriveVersioningFeature implements Versioning {
    private static final Logger log = LogManager.getLogger(DriveVersioningFeature.class);

    protected static final String DEFAULT_FIELDS = String.format("revisions(%s),nextPageToken", "id,md5Checksum,modifiedTime,size,lastModifyingUser");

    private final DriveSession session;
    private final DriveFileIdProvider fileid;

    public DriveVersioningFeature(final DriveSession session, final DriveFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path container) {
        return new VersioningConfiguration(true);
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public boolean isRevertable(final Path file) {
        return false;
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> versions = new AttributedList<>();
            String page = null;
            do {
                final RevisionList list = session.getClient().revisions().list(fileid.getFileId(file))
                        .setFields(DEFAULT_FIELDS)
                        .setPageSize(new HostPreferences(session.getHost()).getInteger("googledrive.list.limit"))
                        .setPageToken(page).execute();
                for(Iterator<Revision> iter = list.getRevisions().iterator(); iter.hasNext(); ) {
                    final Revision revision = iter.next();
                    if(!iter.hasNext()) {
                        // Skip latest revision equals current version
                        if(revision.getMd5Checksum().equals(file.attributes().getChecksum().hash)) {
                            continue;
                        }
                    }
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Found revision %s", revision));
                    }
                    versions.add(new Path(file).withAttributes(this.toAttributes(revision)));
                }
                page = list.getNextPageToken();
            }
            while(page != null);
            return versions.filter(new TimestampComparator(false));
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
        }
    }

    private PathAttributes toAttributes(final Revision f) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(f.getSize());
        if(f.getModifiedTime() != null) {
            attributes.setModificationDate(f.getModifiedTime().getValue());
        }
        attributes.setChecksum(Checksum.parse(f.getMd5Checksum()));
        // The ID of the revision
        attributes.setVersionId(f.getId());
        attributes.setDuplicate(true);
        // The last user to modify this revision
        attributes.setOwner(f.getLastModifyingUser().getDisplayName());
        return attributes;
    }
}
