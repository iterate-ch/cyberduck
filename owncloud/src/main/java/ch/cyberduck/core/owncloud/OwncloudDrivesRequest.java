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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;

/**
 * Query storage spaces of the user authenticated with the current credentials
 */
public class OwncloudDrivesRequest extends HttpGet {

    public OwncloudDrivesRequest(final Host host) {
        super(String.format("%s%s/me/drives",
                new HostUrlProvider().withUsername(false).withPath(false).get(host),
                PathNormalizer.normalize(HostPreferencesFactory.get(host).getProperty("owncloud.root.graph"))
        ));
        this.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
    }
}
