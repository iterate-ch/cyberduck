package ch.cyberduck.core.serializer.impl.dd;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.serializer.HostDictionary;

import com.dd.plist.NSDictionary;

public class HostPlistReader extends PlistReader<Host> {

    private DeserializerFactory deserializer;

    public HostPlistReader() {
        this.deserializer = new DeserializerFactory();
    }

    public HostPlistReader(final DeserializerFactory deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public Host deserialize(final NSDictionary dict) {
        return new HostDictionary(deserializer).deserialize(dict);
    }
}