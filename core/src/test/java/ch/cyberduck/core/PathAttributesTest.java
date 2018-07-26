package ch.cyberduck.core;

import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.serializer.PathAttributesDictionary;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class PathAttributesTest {

    @Test
    public void testCopy() throws Exception {
        final PathAttributes attributes = new PathAttributes();
        final PathAttributes clone = new PathAttributes(attributes);

        assertEquals(clone.getPermission(), attributes.getPermission());
        assertEquals(clone.getModificationDate(), attributes.getModificationDate());
    }

    @Test
    public void testPermissions() throws Exception {
        final PathAttributes attributes = new PathAttributes();
        assertNull(attributes.getOwner());
        assertNull(attributes.getGroup());
        assertNotNull(attributes.getPermission());
        assertEquals(Permission.EMPTY, attributes.getPermission());
        assertEquals(Acl.EMPTY, attributes.getAcl());
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

    @Test
    public void testSerialize() throws Exception {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(100);
        attributes.setModificationDate(System.currentTimeMillis());
        attributes.setPermission(new Permission("644"));
        final Acl acl = new Acl();
        acl.addAll(new Acl.CanonicalUser("user1"), new Acl.Role(Acl.Role.READ), new Acl.Role(Acl.Role.WRITE));
        acl.addAll(new Acl.CanonicalUser("user2"), new Acl.Role(Acl.Role.FULL));
        attributes.setAcl(acl);
        attributes.setChecksum(new Checksum(HashAlgorithm.crc32, "abcdefab"));
        attributes.setVersionId("v-1");
        attributes.setDuplicate(true);
        attributes.setRegion("region");
        attributes.setStorageClass("storageClass");

        final PathAttributes deserialized = new PathAttributesDictionary().deserialize(attributes.serialize(SerializerFactory.get()));
        assertEquals(attributes.getSize(), deserialized.getSize());
        assertEquals(attributes.getModificationDate(), deserialized.getModificationDate());
        assertEquals(attributes.getPermission(), deserialized.getPermission());
        assertTrue(CollectionUtils.isEqualCollection(acl.asList(), deserialized.getAcl().asList()));
        assertEquals(attributes.getChecksum(), deserialized.getChecksum());
        assertEquals(attributes.getVersionId(), deserialized.getVersionId());
        assertEquals(attributes.isDuplicate(), deserialized.isDuplicate());
        assertEquals(attributes.getRegion(), deserialized.getRegion());
        assertEquals(attributes.getStorageClass(), deserialized.getStorageClass());
    }
}
