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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.SerializerFactory;
import ch.cyberduck.core.StaticPermission;
import ch.cyberduck.core.io.Checksum;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PathAttributesDictionaryTest {

    @Test
    public void testSerialize() {
        PathAttributes attributes = new DefaultPathAttributes();
        attributes.setOwner("u");
        attributes.setGroup("g");
        attributes.setTrashed(true);
        attributes.setHidden(true);
        attributes.setModificationDate(System.currentTimeMillis());
        attributes.setPermission(new StaticPermission(Permission.Action.none, Permission.Action.write, Permission.Action.execute));
        PathAttributes clone = new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get()));
        assertEquals(clone.getPermission(), attributes.getPermission());
        assertEquals(clone.getModificationDate(), attributes.getModificationDate());
        assertEquals("u", clone.getOwner());
        assertEquals("g", clone.getGroup());
        assertTrue(clone.isHidden());
        assertTrue(clone.isTrashed());
        assertFalse(clone.isDuplicate());
    }

    @Test
    public void testSerializeMetadata() {
        final PathAttributes attributes = new DefaultPathAttributes();
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("user_tags", "Red%0A6,Work");
        metadata.put("Content-Type", "text/plain");
        attributes.setMetadata(metadata);
        final PathAttributes clone = new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get()));
        assertEquals(metadata, clone.getMetadata());
        // Not mixed with custom properties
        assertTrue(clone.getCustom().isEmpty());
    }

    @Test
    public void testSerializeMetadataAndCustom() {
        final PathAttributes attributes = new DefaultPathAttributes();
        attributes.setMetadata(Collections.singletonMap("user_tags", "Work"));
        attributes.setCustom(Collections.singletonMap("set_tags", "true"));
        final PathAttributes clone = new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get()));
        assertEquals(Collections.singletonMap("user_tags", "Work"), clone.getMetadata());
        assertEquals(Collections.singletonMap("set_tags", "true"), clone.getCustom());
    }

    @Test
    public void testSerializeNoMetadata() {
        final PathAttributes clone = new PathAttributesDictionary<>().deserialize(new DefaultPathAttributes()
                .setCustom(Collections.singletonMap("k", "v")).serialize(SerializerFactory.get()));
        assertTrue(clone.getMetadata().isEmpty());
    }

    @Test
    public void testGetAsDictionary() {
        PathAttributes attributes = new DefaultPathAttributes();
        attributes.setSize(3L);
        attributes.setChecksum(Checksum.parse("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
        attributes.setModificationDate(5343L);
        attributes.setLink(new DescriptiveUrl("https://cyberduck.io/", DescriptiveUrl.Type.signed));
        final PathAttributes deserialized = new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get()));
        assertEquals(attributes, deserialized);
        assertEquals(attributes.hashCode(), deserialized.hashCode());
        assertEquals(attributes.getLink(), deserialized.getLink());
        assertEquals(attributes.getLink().getType(), deserialized.getLink().getType());
    }

    @Test
    public void testSerializeHashCode() {
        PathAttributes attributes = new DefaultPathAttributes();
        attributes.setPermission(new StaticPermission(644));
        attributes.setDuplicate(true);
        attributes.setVersionId("v-1");
        attributes.setFileId("myUniqueId");
        attributes.setDisplayname("myShinyNameOnDisplay");
        attributes.setModificationDate(System.currentTimeMillis());
        assertEquals(attributes, new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get())));
        assertEquals(attributes.hashCode(), new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get())).hashCode());
    }
}
