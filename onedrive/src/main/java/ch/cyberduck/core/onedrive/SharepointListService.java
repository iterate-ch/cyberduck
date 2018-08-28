package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;

public class SharepointListService implements ListService {
    private final SharepointSession session;
    private final IdProvider idProvider;

    public SharepointListService(final SharepointSession session, final IdProvider idProvider) {
        this.session = session;
        this.idProvider = idProvider;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            final AttributedList<Path> list = new AttributedList<>();
            list.add(SharepointSession.DEFAULT_NAME);
            list.add(SharepointSession.GROUPS_NAME);
            listener.chunk(directory, list);
            return list;
        }
        else {
            if(SharepointSession.DEFAULT_NAME.equals(directory)) {
                return new GraphDrivesListService(session).list(directory, listener);
            }
            else if(SharepointSession.GROUPS_NAME.equals(directory)) {
                return new SharepointGroupListService(session).list(directory, listener);
            }
            return new SharepointItemListService().list(directory, listener);
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        idProvider.withCache(cache);
        return this;
    }
}
