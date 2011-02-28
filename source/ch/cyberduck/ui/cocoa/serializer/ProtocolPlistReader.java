package ch.cyberduck.ui.cocoa.serializer;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.Profile;
import ch.cyberduck.core.serializer.ProtocolReaderFactory;
import ch.cyberduck.core.serializer.Reader;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

/**
 * @version $Id$
 */
public class ProtocolPlistReader extends PlistReader<Profile> {

    public static void register() {
        ProtocolReaderFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends ProtocolReaderFactory {
        @Override
        public Reader<Profile> create() {
            return new ProtocolPlistReader();
        }
    }

    @Override
    public Profile deserialize(NSDictionary dict) {
        return new Profile(dict);
    }
}