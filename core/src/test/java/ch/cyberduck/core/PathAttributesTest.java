package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class PathAttributesTest {

    @Test
    public void testCopy() throws Exception {
        PathAttributes attributes = new PathAttributes();
        PathAttributes clone = new PathAttributes(attributes);

        assertEquals(clone.getPermission(), attributes.getPermission());
        assertEquals(clone.getModificationDate(), attributes.getModificationDate());
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
    public void testEquals() throws Exception {
        assertTrue(new PathAttributes().equals(new PathAttributes()));
        final PathAttributes r1 = new PathAttributes();
        r1.setRegion("r1");
        final PathAttributes r2 = new PathAttributes();
        r2.setRegion("r2");
        assertFalse(r1.equals(r2));
    }
}
