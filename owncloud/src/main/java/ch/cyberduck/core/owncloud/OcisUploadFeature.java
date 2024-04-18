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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.tus.TusCapabilities;
import ch.cyberduck.core.tus.TusUploadFeature;

import org.apache.http.client.HttpClient;

public class OcisUploadFeature extends TusUploadFeature {

    public OcisUploadFeature(final OwncloudSession session, final Write<Void> writer, final TusCapabilities capabilities) {
        this(session.getHost(), session.getClient().getClient(), writer, capabilities);
    }

    public OcisUploadFeature(final Host host, final HttpClient client, final Write<Void> writer, final TusCapabilities capabilities) {
        super(host, client, writer, capabilities);
    }
}
