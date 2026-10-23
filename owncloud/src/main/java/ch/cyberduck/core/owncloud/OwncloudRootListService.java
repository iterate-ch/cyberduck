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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.nextcloud.NextcloudListService;

/**
 * List virtual folders for personal space, project spaces and shares in root when spaces are supported
 */
public class OwncloudRootListService implements ListService {

    private final OwncloudSession session;
    private final ListService delegate;

    public OwncloudRootListService(final OwncloudSession session) {
        this(session, new NextcloudListService(session));
    }

    /**
     * @param delegate List service for folders within spaces or without support for spaces
     */
    public OwncloudRootListService(final OwncloudSession session, final ListService delegate) {
        this.session = session;
        this.delegate = delegate;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(!session.ocs.spaces) {
            return delegate.list(directory, listener);
        }
        if(directory.isRoot()) {
            final AttributedList<Path> list = new AttributedList<>();
            if(null != session.mapper.getPersonal()) {
                // No personal space for users with role User Light
                list.add(OwncloudPathMapper.PERSONAL_FOLDER);
            }
            if(session.ocs.projects) {
                list.add(OwncloudPathMapper.SPACES_FOLDER);
            }
            if(session.ocs.sharejail) {
                list.add(OwncloudPathMapper.SHARES_FOLDER);
            }
            listener.chunk(directory, list);
            return list;
        }
        if(new SimplePathPredicate(OwncloudPathMapper.SPACES_FOLDER).test(directory)) {
            return new OwncloudDrivesListService(session).list(directory, listener);
        }
        return delegate.list(directory, listener);
    }
}
