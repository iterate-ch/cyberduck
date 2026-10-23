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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;

public class OwncloudSpacesAttributesFinderFeature extends OwncloudAttributesFinderFeature {

    private final OwncloudSpacesPathContainerService containerService = new OwncloudSpacesPathContainerService();

    public OwncloudSpacesAttributesFinderFeature(final OwncloudSession session) {
        super(session);
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(containerService.isVirtual(file)) {
            return PathAttributes.EMPTY;
        }
        return super.find(file, listener);
    }
}
