package ch.cyberduck.core.local;

import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.Permission;

import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.*;

public class LocalAttributesTest {

    @Test
    public void testGetSize() throws Exception {
        assertEquals(-1, new LocalAttributes(UUID.randomUUID().toString()).getSize());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        LocalAttributes a = new LocalAttributes(f.getAbsolutePath());
        assertEquals(0, a.getSize());
        f.delete();
    }

    @Test
    public void testGetPermission() throws Exception {
        final LocalAttributes a = new LocalAttributes(UUID.randomUUID().toString());
        assertNotNull(a.getPermission());
        assertEquals(Permission.EMPTY, a.getPermission());
        assertSame(a.getPermission(), a.getPermission());
    }

    @Test
    public void testGetCreationDate() throws Exception {
        assertEquals(-1, new LocalAttributes(UUID.randomUUID().toString()).getCreationDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        LocalAttributes a = new LocalAttributes(f.getAbsolutePath());
        assertTrue(a.getCreationDate() > 0);
        f.delete();
    }

    @Test
    public void testGetAccessedDate() throws Exception {
        assertEquals(-1, new LocalAttributes(UUID.randomUUID().toString()).getAccessedDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        LocalAttributes a = new LocalAttributes(f.getAbsolutePath());
        assertTrue(a.getAccessedDate() > 0);
        f.delete();
    }

    @Test
    public void getGetModificationDate() throws Exception {
        assertEquals(-1, new LocalAttributes(UUID.randomUUID().toString()).getModificationDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        LocalAttributes a = new LocalAttributes(f.getAbsolutePath());
        assertTrue(a.getModificationDate() > 0);
        f.delete();
    }

    @Test
    public void testGetOwner() throws Exception {
        LocalAttributes a = new LocalAttributes(UUID.randomUUID().toString());
        assertNull(a.getOwner());
    }

    @Test
    public void testGetGroup() throws Exception {
        LocalAttributes a = new LocalAttributes(UUID.randomUUID().toString());
        assertNull(a.getGroup());
    }

    @Test
    public void testIsBundle() throws Exception {
        LocalAttributes a = new LocalAttributes(UUID.randomUUID().toString());
        assertFalse(a.isBundle());
    }

    @Test
    public void testGetChecksum() throws Exception {
        assertNull(new LocalAttributes(UUID.randomUUID().toString()).getChecksum());
    }
}
