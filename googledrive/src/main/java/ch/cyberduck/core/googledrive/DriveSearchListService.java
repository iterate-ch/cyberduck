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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.preferences.HostPreferences;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.google.api.services.drive.model.File;

public class DriveSearchListService extends AbstractDriveListService {

    private static final String DEFAULT_FIELDS = String.format("files(%s,parents),nextPageToken", DriveAttributesFinderFeature.DEFAULT_FIELDS);

    private final DriveSession session;
    private final DriveFileIdProvider fileid;
    private final String query;
    private final DriveAttributesFinderFeature attributes;

    public DriveSearchListService(final DriveSession session, final DriveFileIdProvider fileid, final String query) {
        super(session, fileid, new HostPreferences(session.getHost()).getInteger("googledrive.list.limit"), DEFAULT_FIELDS);
        this.session = session;
        this.fileid = fileid;
        this.query = query;
        this.attributes = new DriveAttributesFinderFeature(session, fileid);
    }

    @Override
    protected String query(final Path directory, final ListProgressListener listener) throws BackgroundException {
        // The contains operator only performs prefix matching for a name.
        return String.format("name contains '%s'", query);
    }

    @Override
    protected Set<Path> parents(final Path directory, final File f) throws BackgroundException {
        try {
            // Parent may not be current working directory when searching recursively
            final Set<Path> tree = new HashSet<>();
            if(null == f.getParents()) {
                // Shared files do not have parent set
                tree.add(DriveHomeFinderService.SHARED_FOLDER_NAME);
            }
            else {
                for(String parentId : f.getParents()) {
                    final Set<Path> parents = this.parents(parentId, new ArrayDeque<>());
                    for(Path parent : parents) {
                        if(parent.isChild(directory) || new SimplePathPredicate(parent).test(directory)) {
                            tree.add(parent);
                        }
                    }
                }
            }
            return tree;
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService(fileid).map("Listing directory failed", e, directory);
        }
    }

    /**
     * @return The IDs of the parent folders which contain the file.
     */
    private Set<Path> parents(String fileId, final Deque<File> dequeue) throws IOException {
        final Set<Path> tree = new HashSet<>();
        while(true) {
            final File f = session.getClient().files().get(fileId).setFields(String.format("parents,%s", DriveAttributesFinderFeature.DEFAULT_FIELDS)).execute();
            dequeue.push(f);
            if(null == f.getParents()) {
                break;
            }
            for(String parentid : f.getParents()) {
                tree.addAll(this.parents(parentid, new ArrayDeque<>(dequeue)));
                fileId = parentid;
            }
        }
        Path parent = Home.ROOT;
        while(dequeue.size() > 0) {
            final File f = dequeue.pop();
            parent = new Path(parent, f.getName(), this.toType(f), attributes.toAttributes(f));
        }
        tree.add(parent);
        return tree;
    }
}
