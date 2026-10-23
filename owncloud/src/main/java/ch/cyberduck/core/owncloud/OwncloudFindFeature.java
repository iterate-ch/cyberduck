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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.exception.BackgroundException;

public class OwncloudFindFeature extends DAVFindFeature {

    private final OwncloudPathContainerService containerService = new OwncloudPathContainerService();

    private final OwncloudSession session;

    public OwncloudFindFeature(final OwncloudSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(session.ocs.spaces) {
            if(containerService.isVirtual(file)) {
                return true;
            }
        }
        return super.find(file, listener);
    }
}
