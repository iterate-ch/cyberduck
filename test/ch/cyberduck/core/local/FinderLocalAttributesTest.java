package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class FinderLocalAttributesTest extends AbstractTestCase {

    @Test
    public void testGetSize() throws Exception {
        assertEquals(-1, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getSize());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        assertEquals(0, a.getSize());
        f.delete();
    }

    @Test
    public void testGetPermission() throws Exception {
        assertEquals(Permission.EMPTY, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getPermission());
    }

    @Test
    public void testGetCreationDate() throws Exception {
        assertEquals(-1, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getCreationDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        assertTrue(a.getCreationDate() > 0);
        f.delete();
    }

    @Test
    public void testGetAccessedDate() throws Exception {
        assertEquals(-1, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getAccessedDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        assertTrue(a.getAccessedDate() > 0);
        f.delete();
    }

    @Test
    public void getGetModificationDate() throws Exception {
        assertEquals(-1, new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getModificationDate());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        assertTrue(a.getModificationDate() > 0);
        f.delete();
    }

    @Test
    public void testGetOwner() throws Exception {
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        assertNotNull(a.getOwner());
        f.delete();
    }

    @Test
    public void testGetGroup() throws Exception {
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        assertNotNull(a.getGroup());
        f.delete();
    }

    @Test
    public void testGetInode() throws Exception {
        assertNull(new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).getInode());
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath()));
        assertTrue(a.getInode() > 0);
        f.delete();
    }

    @Test
    public void testIsBundle() throws Exception {
        FinderLocalAttributes a = new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString()));
        assertFalse(a.isBundle());
    }

    @Test
    public void testIsSymbolicLink() throws Exception {
        assertFalse(new FinderLocalAttributes(new FinderLocal(UUID.randomUUID().toString())).isSymbolicLink());
        assertTrue(new FinderLocalAttributes(new FinderLocal("/tmp")).isSymbolicLink());
    }

    @Test
    public void testPermission() throws Exception {
        final File f = new File(UUID.randomUUID().toString());
        f.createNewFile();
        assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isReadable());
        assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isWritable());
        assertFalse(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isExecutable());
        f.delete();
        assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isReadable());
        assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isWritable());
        assertTrue(new FinderLocalAttributes(new FinderLocal(f.getAbsolutePath())).getPermission().isExecutable());
    }
}
