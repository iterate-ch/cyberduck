package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.Permission;

import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class LocalAttributesTest extends AbstractTestCase {

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
        assertEquals(Permission.EMPTY, new LocalAttributes(UUID.randomUUID().toString()).getPermission());
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
        assertEquals("Unknown", a.getOwner());
    }

    @Test
    public void testGetGroup() throws Exception {
        LocalAttributes a = new LocalAttributes(UUID.randomUUID().toString());
        assertEquals("Unknown", a.getGroup());
    }

    @Test
    public void testIsBundle() throws Exception {
        LocalAttributes a = new LocalAttributes(UUID.randomUUID().toString());
        assertFalse(a.isBundle());
    }

    @Test
    public void testGetModificationDate() throws Exception {

    }

    @Test
    public void testGetType() throws Exception {

    }

    @Test
    public void testIsVolume() throws Exception {

    }

    @Test
    public void testIsDirectory() throws Exception {

    }

    @Test
    public void testIsFile() throws Exception {

    }

    @Test
    public void testIsSymbolicLink() throws Exception {
        LocalAttributes a = new LocalAttributes("/t");
        assertFalse(a.isSymbolicLink());
    }

    @Test
    public void testGetChecksum() throws Exception {
        assertNull(new LocalAttributes(UUID.randomUUID().toString()).getChecksum());
    }
}
