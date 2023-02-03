package ch.cyberduck.core;

import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.serializer.PathAttributesDictionary;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PathAttributesTest {

    @Test
    public void testCopy() {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(1L);
        attributes.setQuota(10L);
        attributes.setModificationDate(System.currentTimeMillis());
        attributes.setRevision(2L);
        attributes.setFileId(new AlphanumericRandomStringService().random());
        attributes.setVersionId(new AlphanumericRandomStringService().random());
        attributes.setDuplicate(true);
        attributes.setLockId(new AlphanumericRandomStringService().random());
        attributes.setPermission(new Permission(644));
        final PathAttributes clone = new PathAttributes(attributes);
        assertEquals(clone.getPermission(), attributes.getPermission());
        assertEquals(clone.getModificationDate(), attributes.getModificationDate());
        assertEquals(clone, attributes);
        attributes.setSize(2L);
        assertEquals(1L, clone.getSize());
        attributes.setVersionId("b");
        assertNotEquals(attributes.getVersionId(), clone.getVersionId());
        assertEquals(attributes.getPermission(), clone.getPermission());
        assertNotSame(attributes.getPermission(), clone.getPermission());
        attributes.setLink(new DescriptiveUrl(URI.create("http://g")));
        assertEquals(DescriptiveUrl.EMPTY, clone.getLink());
    }

    @Test
    public void testPermissions() {
        final PathAttributes attributes = new PathAttributes();
        assertNull(attributes.getOwner());
        assertNull(attributes.getGroup());
        assertNotNull(attributes.getPermission());
        assertEquals(Permission.EMPTY, attributes.getPermission());
        assertEquals(Acl.EMPTY, attributes.getAcl());
    }

    @Test
    public void testEquals() {
        assertEquals(new PathAttributes(), new PathAttributes());
        final PathAttributes r1 = new PathAttributes();
        r1.setVersionId("r1");
        final PathAttributes r2 = new PathAttributes();
        r2.setVersionId("r2");
        assertNotEquals(r1, r2);
    }

    @Test
    public void testSerialize() {
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
        attributes.setFileId("f-1");
        attributes.setDuplicate(true);
        attributes.setRegion("region");
        attributes.setStorageClass("storageClass");
        final Map<String, String> custom = new HashMap<>(attributes.getCustom());
        custom.put("key", "value");
        attributes.setCustom(custom);
        final PathAttributes deserialized = new PathAttributesDictionary<>().deserialize(attributes.serialize(SerializerFactory.get()));
        assertEquals(attributes.getSize(), deserialized.getSize());
        assertEquals(attributes.getModificationDate(), deserialized.getModificationDate());
        assertEquals(attributes.getPermission(), deserialized.getPermission());
        assertTrue(CollectionUtils.isEqualCollection(acl.asList(), deserialized.getAcl().asList()));
        assertEquals(attributes.getChecksum(), deserialized.getChecksum());
        assertEquals(attributes.getVersionId(), deserialized.getVersionId());
        assertEquals(attributes.getFileId(), deserialized.getFileId());
        assertEquals(attributes.isDuplicate(), deserialized.isDuplicate());
        assertEquals(attributes.getRegion(), deserialized.getRegion());
        assertEquals(attributes.getStorageClass(), deserialized.getStorageClass());
        assertEquals(attributes.getCustom(), deserialized.getCustom());
        assertEquals(attributes, deserialized);
    }
}
