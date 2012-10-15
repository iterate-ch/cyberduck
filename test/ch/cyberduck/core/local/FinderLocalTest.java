package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Permission;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class FinderLocalTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        FinderLocal.register();
    }

    @Test
    public void testEqual() throws Exception {
        final String name = UUID.randomUUID().toString();
        Local l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        l.touch();
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        final FinderLocal other = new FinderLocal(System.getProperty("java.io.tmpdir"), name + "-");
        assertNotSame(other, l);
        other.touch();
        assertNotSame(other, l);
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
}
