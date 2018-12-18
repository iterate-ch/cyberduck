package ch.cyberduck.core.dav;

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

import ch.cyberduck.core.Path;

import java.io.IOException;
import java.util.List;

import com.github.sardine.DavResource;

public class DAVListIISService extends DAVListService {

    private final DAVSession session;

    public DAVListIISService(final DAVSession session, final DAVAttributesFinderFeature attributes) {
        super(session, attributes);
        this.session = session;
    }

    @Override
    protected List<DavResource> list(final Path directory) throws IOException {
        return session.getClient().list(new DAVPathEncoder().encode(directory), 1, true);
    }
}
