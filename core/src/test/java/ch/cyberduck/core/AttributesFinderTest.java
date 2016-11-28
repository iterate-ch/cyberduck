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

import ch.cyberduck.core.serializer.PathAttributesDictionary;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AttributesFinderTest {

    @Test
    public void testClone() throws Exception {
        PathAttributes attributes = new PathAttributes();
        PathAttributes clone = new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get()));

        assertEquals(clone.getPermission(), attributes.getPermission());
        assertEquals(clone.getModificationDate(), attributes.getModificationDate());
    }
}
