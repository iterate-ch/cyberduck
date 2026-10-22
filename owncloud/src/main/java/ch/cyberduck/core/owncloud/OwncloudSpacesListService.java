package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.owncloud.model.Drives;

import java.util.EnumSet;

/**
 * List virtual folders for personal space, project spaces and shares in root
 */
public class OwncloudSpacesListService implements ListService {

    private final OwncloudSession session;
    private final ListService delegate;

    public OwncloudSpacesListService(final OwncloudSession session, final ListService delegate) {
        this.session = session;
        this.delegate = delegate;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            final AttributedList<Path> list = new AttributedList<>();
            list.add(OwncloudSpaces.PERSONAL_FOLDER);
            if(session.ocs.projects) {
                list.add(OwncloudSpaces.SPACES_FOLDER);
            }
            if(session.ocs.sharejail) {
                list.add(OwncloudSpaces.SHARES_FOLDER);
            }
            listener.chunk(directory, list);
            return list;
        }
        if(new SimplePathPredicate(OwncloudSpaces.SPACES_FOLDER).test(directory)) {
            final AttributedList<Path> list = new AttributedList<>();
            for(Drives.Drive drive : session.drives()) {
                list.add(new Path(directory, drive.name, EnumSet.of(Path.Type.directory, Path.Type.volume),
                        new DefaultPathAttributes().setFileId(drive.root.id)));
            }
            listener.chunk(directory, list);
            return list;
        }
        return delegate.list(directory, listener);
    }
}
