package ch.cyberduck.ui.cocoa.controller;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostFilter;

public final class NSDictionaryHostFilter implements HostFilter {

    private final NSObject uuid;

    public NSDictionaryHostFilter(final NSDictionary item) {
        this(item.objectForKey("UUID"));
    }

    public NSDictionaryHostFilter(final NSObject uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean accept(final Host host) {
        return host.getUuid().equals(uuid.toString());
    }
}
