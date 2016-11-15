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

    private final boolean username;

    private final boolean path;

    public HostUrlProvider() {
        this(true, false);
    }

    public HostUrlProvider(final boolean username) {
        this(username, false);
    }

    public HostUrlProvider(final boolean username, final boolean path) {
        this.username = username;
        this.path = path;
    }

    /**
     * @return URL
     */
    public String get(final Host host) {
        final String base = String.format("%s://%s%s%s",
                host.getProtocol().getScheme().toString(),
                username && StringUtils.isNotEmpty(host.getCredentials().getUsername()) ? String.format("%s@", host.getCredentials().getUsername()) : "",
                new PunycodeConverter().convert(host.getHostname()),
                host.getPort() != host.getProtocol().getScheme().getPort() ? String.format(":%d", host.getPort()) : "");
        if(path) {
            if(StringUtils.isNotBlank(host.getDefaultPath())) {
                return String.format("%s%s", base, PathNormalizer.normalize(host.getDefaultPath()));
            }
        }
        return base;
    }
}
