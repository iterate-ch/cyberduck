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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.nextcloud.NextcloudShareFeature;

/**
 * Sharing is only supported for files in personal space
 */
public class OwncloudSpacesShareFeature extends NextcloudShareFeature {

    public OwncloudSpacesShareFeature(final DAVSession session) {
        super(session);
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        if(!file.isChild(OwncloudSpaces.PERSONAL_FOLDER)) {
            return false;
        }
        return super.isSupported(file, type);
    }

    @Override
    protected Path home() {
        return OwncloudSpaces.PERSONAL_FOLDER;
    }
}
