package ch.cyberduck.core.serializer;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.SerializerFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PathAttributesDictionaryTest {

    @Test
    public void testSerialize() throws Exception {
        PathAttributes attributes = new PathAttributes();
        PathAttributes clone = new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get()));

        assertEquals(clone.getPermission(), attributes.getPermission());
        assertEquals(clone.getModificationDate(), attributes.getModificationDate());
    }

    @Test
    public void testGetAsDictionary() throws Exception {
        PathAttributes attributes = new PathAttributes();
        attributes.setSize(3L);
        attributes.setModificationDate(5343L);
        assertEquals(attributes, new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get())));
        assertEquals(attributes.hashCode(), new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get())).hashCode());
    }

    @Test
    public void testSerializeHashCode() throws Exception {
        PathAttributes attributes = new PathAttributes();
        attributes.setPermission(new Permission(644));
        attributes.setDuplicate(true);
        attributes.setVersionId("v-1");
        attributes.setModificationDate(System.currentTimeMillis());
        assertEquals(attributes, new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get())));
        assertEquals(attributes.hashCode(), new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get())).hashCode());
    }
}