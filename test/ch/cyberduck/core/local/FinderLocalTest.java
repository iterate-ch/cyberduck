package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Permission;

import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FinderLocalTest extends AbstractTestCase {

    @Test
    public void testEqual() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        l.touch();
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        final FinderLocal other = new FinderLocal(System.getProperty("java.io.tmpdir"), name + "-");
        assertNotSame(other, l);
        other.touch();
        assertNotSame(other, l);
    }

    @Test
    public void testList() throws Exception {
        assertFalse(new FinderLocal("profiles").list().isEmpty());
    }

    @Test
    public void testTilde() throws Exception {
        assertEquals(System.getProperty("user.home") + "/f", new FinderLocal("~/f").getAbsolute());
        assertEquals("~/f", new FinderLocal("~/f").getAbbreviatedPath());
    }

    @Test
    public void testDisplayName() throws Exception {
        assertEquals("f/a", new FinderLocal(System.getProperty("java.io.tmpdir"), "f:a").getDisplayName());
    }

    @Test
    public void testTrash() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        l.touch();
        assertTrue(l.exists());
        l.trash();
        assertFalse(l.exists());
    }

    @Test
    public void testTrashRepeated() throws Exception {
        this.repeat(new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                Local l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
                l.touch();
                assertTrue(l.exists());
                l.trash();
                assertFalse(l.exists());
                return l;
            }
        }, 10);
    }

    @Test
    public void testWriteUnixPermission() throws Exception {
        this.repeat(new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                Local l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
                l.touch();
                final Permission permission = new Permission(644);
                l.writeUnixPermission(permission);
                assertEquals(permission, l.attributes().getPermission());
                l.delete();
                return l;
            }
        }, 10);
    }

    @Test
    public void testTouch() {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir") + "/p/", UUID.randomUUID().toString());
        l.touch();
        assertTrue(l.exists());
        l.touch();
        assertTrue(l.exists());
        l.delete();
    }

    @Test
    public void testMkdir() {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir") + "/p/", UUID.randomUUID().toString());
        l.mkdir();
        assertTrue(l.exists());
        l.mkdir();
        assertTrue(l.exists());
        l.delete();
    }

    @Test
    public void testToUrl() throws Exception {
        assertEquals("file:/c/file", new FinderLocal("/c/file").toURL());
    }

    @Test
    public void testBookmark() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir") + "/p/", UUID.randomUUID().toString());
        assertNull(l.getBookmark());
        l.touch();
        assertNotNull(l.getBookmark());
    }
}
