package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.AbstractHomeFeature;

import java.util.EnumSet;

public class NextcloudHomeFeature extends AbstractHomeFeature {

    private final Host bookmark;

    public NextcloudHomeFeature(final Host bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public Path find() throws BackgroundException {
        return new Path(new Path(bookmark.getProtocol().getDefaultPath(), EnumSet.of(Path.Type.directory)),
            URIEncoder.encode(bookmark.getCredentials().getUsername()), EnumSet.of(Path.Type.directory));
    }
}
