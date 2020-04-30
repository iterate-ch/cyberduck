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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.apache.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.users.DbxUserUsersRequests;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxHomeFinderFeature extends DefaultHomeFinderService {
    private static final Logger log = Logger.getLogger(DropboxRootListService.class);

    private final DropboxSession session;

    public DropboxHomeFinderFeature(final DropboxSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        final Path directory = super.find();
        if(directory.isRoot()) {
            try {
                final FullAccount account = new DbxUserUsersRequests(session.getClient()).getCurrentAccount();
                switch(account.getAccountType()) {
                    case BUSINESS:
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Set root namespace %s", account.getRootInfo().getRootNamespaceId()));
                        }
                        directory.attributes().withVersionId(account.getRootInfo().getRootNamespaceId());
                }
            }
            catch(DbxException e) {
                throw new DropboxExceptionMappingService().map(e);
            }
        }
        return directory;
    }
}
