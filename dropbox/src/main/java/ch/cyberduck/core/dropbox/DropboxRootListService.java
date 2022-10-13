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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.users.DbxUserUsersRequests;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxRootListService implements ListService {
    private static final Logger log = LogManager.getLogger(DropboxRootListService.class);

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
                        if(log.isInfoEnabled()) {
                            log.info("Connect to business account type");
                        }
                        return new DropboxListService(session).list(
                                directory.withAttributes(new PathAttributes().withFileId(account.getRootInfo().getRootNamespaceId())), listener);
                }
            }
            return new DropboxListService(session).list(directory, listener);
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
