package ch.cyberduck.ui.cocoa.serializer;

/*
 * Copyright (c) 2009 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.serializer.HostReaderFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

/**
 * @version $Id$
 */
public class HostPlistReader extends PlistReader<Host> {

    public static void register() {
        HostReaderFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends HostReaderFactory {
        @Override
        public Reader<Host> create() {
            return new HostPlistReader();
        }
    }

    @Override
    public Host deserialize(NSDictionary dict) {
        return new Host(dict);
    }
}