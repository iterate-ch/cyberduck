package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.tus.TusCapabilities;
import ch.cyberduck.core.tus.TusUploadFeature;

public class OcisUploadFeature extends TusUploadFeature {

    private final OwncloudSession session;

    public OcisUploadFeature(final OwncloudSession session, final TusCapabilities capabilities) {
        super(session, capabilities);
        this.session = session;
    }

    @Override
    protected String toCreationUrl(final Path directory) {
        return String.format("%s%s", new HostUrlProvider().withUsername(false).get(session.getHost()),
                new DAVPathEncoder(session).encode(directory));
    }
}
