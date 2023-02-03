package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.exception.NotfoundException;

import java.io.IOException;
import java.util.Optional;

import com.github.sardine.DavResource;

public class OwncloudTimestampFeature extends DAVTimestampFeature {

    private final OwncloudSession session;

    public OwncloudTimestampFeature(final OwncloudSession session) {
        super(session);
        this.session = session;
    }

    @Override
    protected DavResource getResource(final Path file) throws NotfoundException, IOException {
        final Optional<DavResource> optional = new OwncloudAttributesFinderFeature(session).list(file).stream().findFirst();
        if(!optional.isPresent()) {
            throw new NotfoundException(file.getAbsolute());
        }
        return optional.get();
    }
}
