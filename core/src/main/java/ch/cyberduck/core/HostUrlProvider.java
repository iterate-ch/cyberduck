package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.idna.PunycodeConverter;

import org.apache.commons.lang3.StringUtils;

public class HostUrlProvider {

    private boolean includeUsername;
    private boolean includePath;

    public HostUrlProvider() {
        this(true, false);
    }

    public HostUrlProvider(final boolean includeUsername) {
        this(includeUsername, false);
    }

    public HostUrlProvider(final boolean includeUsername, final boolean includePath) {
        this.includeUsername = includeUsername;
        this.includePath = includePath;
    }

    public HostUrlProvider withUsername(final boolean include) {
        this.includeUsername = include;
        return this;
    }

    public HostUrlProvider withPath(final boolean include) {
        this.includePath = include;
        return this;
    }

    /**
     * @return URL
     */
    public String get(final Host bookmark) {
        final Scheme scheme = bookmark.getProtocol().getScheme();
        final int port = bookmark.getPort();
        final String username = bookmark.getCredentials().getUsername();
        final String hostname = HostnameConfiguratorFactory.get(bookmark.getProtocol()).getHostname(bookmark.getHostname());
        final String path = bookmark.getDefaultPath();
        return this.get(scheme, port, username, hostname, path);
    }

    public String get(final Scheme scheme, final int port, final String username, final String hostname, final String path) {
        final String base = String.format("%s://%s%s%s",
            scheme,
            includeUsername && StringUtils.isNotEmpty(username) ? String.format("%s@", URIEncoder.encode(username)) : "",
            new PunycodeConverter().convert(hostname),
            port != scheme.getPort() ? String.format(":%d", port) : "");
        if(includePath) {
            if(StringUtils.isNotBlank(path)) {
                return String.format("%s%s", base, PathNormalizer.normalize(path));
            }
        }
        return base;
    }
}
