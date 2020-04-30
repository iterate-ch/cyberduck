package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.users.DbxUserUsersRequests;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxRootListService implements ListService {
    private static final Logger log = Logger.getLogger(DropboxRootListService.class);

    private final DropboxSession session;

    public DropboxRootListService(final DropboxSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            if(directory.isRoot()) {
                final FullAccount account = new DbxUserUsersRequests(session.getClient()).getCurrentAccount();
                switch(account.getAccountType()) {
                    case BUSINESS:
                        return new DropboxListService(session).list(directory, new HomeNamespaceListProgressListener(listener, account));
                }
            }
            return new DropboxListService(session).list(directory, listener);
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }

    private static final class HomeNamespaceListProgressListener extends IndexedListProgressListener {
        private final ListProgressListener listener;
        private final FullAccount account;

        public HomeNamespaceListProgressListener(final ListProgressListener listener, final FullAccount account) {
            this.listener = listener;
            this.account = account;
        }

        @Override
        public void chunk(final Path folder, final AttributedList<Path> list) throws ConnectionCanceledException {
            super.chunk(folder, list);
            listener.chunk(folder, list);
        }

        @Override
        public void visit(final AttributedList<Path> list, final int index, final Path file) {
            if(StringUtils.isBlank(file.attributes().getVersionId())) {
                // User home folder does not have a id set
                file.attributes().setVersionId(account.getRootInfo().getHomeNamespaceId());
            }
        }

        @Override
        public void message(final String message) {
            listener.message(message);
        }
    }
}
