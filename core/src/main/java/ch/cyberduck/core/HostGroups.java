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

import java.util.Collections;
import java.util.Set;

public interface HostGroups {
    Set<String> groups(Host host);

    HostGroups NONE = new HostGroups() {
        @Override
        public Set<String> groups(final Host host) {
            return Collections.emptySet();
        }
    };

    HostGroups PROVIDER = new HostGroups() {
        @Override
        public Set<String> groups(final Host host) {
            return Collections.singleton(host.getProtocol().getProvider());
        }
    };

    HostGroups LABELS = Host::getLabels;
}
