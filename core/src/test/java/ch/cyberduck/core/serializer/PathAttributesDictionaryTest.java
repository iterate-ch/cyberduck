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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.io.Checksum;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class PathAttributesDictionaryTest {

    @Test
    public void testSerialize() {
        PathAttributes attributes = new PathAttributes();
        attributes.setOwner("u");
        attributes.setGroup("g");
        attributes.setModificationDate(System.currentTimeMillis());
        attributes.setPermission(new Permission(Permission.Action.none, Permission.Action.write, Permission.Action.execute));
        PathAttributes clone = new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get()));
        assertEquals(clone.getPermission(), attributes.getPermission());
        assertEquals(clone.getModificationDate(), attributes.getModificationDate());
        assertEquals("u", clone.getOwner());
        assertEquals("g", clone.getGroup());
    }

    @Test
    public void testGetAsDictionary() {
        PathAttributes attributes = new PathAttributes();
        attributes.setSize(3L);
        attributes.setChecksum(Checksum.parse("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
        attributes.setModificationDate(5343L);
        attributes.setLink(new DescriptiveUrl(URI.create("https://cyberduck.io/"), DescriptiveUrl.Type.signed));
        final PathAttributes deserialized = new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get()));
        assertEquals(attributes, deserialized);
        assertEquals(attributes.hashCode(), deserialized.hashCode());
        assertEquals(attributes.getLink(), deserialized.getLink());
        assertEquals(attributes.getLink().getType(), deserialized.getLink().getType());
    }

    @Test
    public void testSerializeHashCode() {
        PathAttributes attributes = new PathAttributes();
        attributes.setPermission(new Permission(644));
        attributes.setDuplicate(true);
        attributes.setVersionId("v-1");
        attributes.setFileId("myUniqueId");
        attributes.setModificationDate(System.currentTimeMillis());
        assertEquals(attributes, new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get())));
        assertEquals(attributes.hashCode(), new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get())).hashCode());
    }
}
