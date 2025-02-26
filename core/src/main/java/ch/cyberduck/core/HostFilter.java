package ch.cyberduck.core;

/*
 *  Copyright (c) 2006 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.StringUtils;

public interface HostFilter {
    boolean accept(Host host);

    HostFilter NONE = new HostFilter() {
        @Override
        public boolean accept(final Host host) {
            return true;
        }
    };

    HostFilter BYPROTOCOL = new HostFilter() {
        @Override
        public boolean accept(final Host host) {
            final String filter = HostPreferencesFactory.get(host).getProperty("bookmark.filter.protocol.type");
            if(StringUtils.isBlank(filter)) {
                return true;
            }
            return Protocol.Type.valueOf(filter).equals(host.getProtocol().getType());
        }
    };
}
