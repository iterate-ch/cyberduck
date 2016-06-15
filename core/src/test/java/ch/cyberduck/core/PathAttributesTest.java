package ch.cyberduck.core;

import ch.cyberduck.core.serializer.PathAttributesDictionary;

import org.junit.Test;

import static org.junit.Assert.*;

public class PathAttributesTest {

    @Test
    public void testGetAsDictionary() throws Exception {
        PathAttributes attributes = new PathAttributes();
        attributes.setSize(3L);
        attributes.setModificationDate(5343L);
        assertEquals(attributes, new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get())));
        assertEquals(attributes.hashCode(), new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get())).hashCode());
    }

    @Test
    public void testPermissions() throws Exception {
        PathAttributes attributes = new PathAttributes();
        assertNull(attributes.getOwner());
        assertNull(attributes.getGroup());
        assertNotNull(attributes.getPermission());
        assertEquals(Permission.EMPTY, attributes.getPermission());
        assertEquals(Acl.EMPTY, attributes.getAcl());
    }

    @Test
    public void testSerialize() throws Exception {
        PathAttributes attributes = new PathAttributes();
        attributes.setPermission(new Permission(644));
        attributes.setDuplicate(true);
        attributes.setVersionId("v-1");
        attributes.setModificationDate(System.currentTimeMillis());
        assertEquals(attributes, new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get())));
        assertEquals(attributes.hashCode(), new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get())).hashCode());
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(new PathAttributes().equals(new PathAttributes()));
        final PathAttributes r1 = new PathAttributes();
        r1.setRegion("r1");
        final PathAttributes r2 = new PathAttributes();
        r2.setRegion("r2");
        assertFalse(r1.equals(r2));
    }
}
