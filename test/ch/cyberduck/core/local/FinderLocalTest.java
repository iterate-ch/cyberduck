package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.test.Depends;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class FinderLocalTest extends AbstractTestCase {

    @Test
    public void testEqual() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        LocalTouchFactory.get().touch(l);
        assertEquals(new FinderLocal(System.getProperty("java.io.tmpdir"), name), l);
        final FinderLocal other = new FinderLocal(System.getProperty("java.io.tmpdir"), name + "-");
        assertNotSame(other, l);
        LocalTouchFactory.get().touch(other);
        assertNotSame(other, l);
    }

    @Test(expected = AccessDeniedException.class)
    public void testReadNoFile() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        l.getInputStream();
    }

    @Test
    public void testNoCaseSensitive() throws Exception {
        final String name = UUID.randomUUID().toString();
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        LocalTouchFactory.get().touch(l);
        assertTrue(l.exists());
        assertTrue(new FinderLocal(System.getProperty("java.io.tmpdir"), StringUtils.upperCase(name)).exists());
        assertTrue(new FinderLocal(System.getProperty("java.io.tmpdir"), StringUtils.lowerCase(name)).exists());
    }

    @Test
    public void testList() throws Exception {
        assertFalse(new FinderLocal("profiles").list().isEmpty());
    }

    @Test(expected = AccessDeniedException.class)
    public void testListNotFound() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        l.list();
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
    public void testWriteUnixPermission() throws Exception {
        this.repeat(new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                Local l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
                new DefaultLocalTouchFeature().touch(l);
                final Permission permission = new Permission(644);
                l.attributes().setPermission(permission);
                assertEquals(permission, l.attributes().getPermission());
                l.delete();
                return l;
            }
        }, 10);
    }

    @Test
    public void testMkdir() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
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
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertNull(l.getBookmark());
        LocalTouchFactory.get().touch(l);
        assertNotNull(l.getBookmark());
        assertEquals(l.getBookmark(), l.getBookmark());
        assertSame(l.getBookmark(), l.getBookmark());
    }

    @Test
    public void testBookmarkSaved() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertNull(l.getBookmark());
        l.setBookmark("a");
        assertEquals("a", l.getBookmark());
        assertNotNull(l.getOutputStream(false));
        assertNotNull(l.getInputStream());
    }

    @Test(expected = NotfoundException.class)
    public void testSymlinkTargetNotfound() throws Exception {
        FinderLocal l = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            assertNull(l.getSymlinkTarget());
        }
        catch(NotfoundException e) {
            assertEquals("File not found", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testSymlinkTarget() throws Exception {
        FinderLocal l = new FinderLocal("/var");
        assertNotNull(l.getSymlinkTarget());
    }

    @Test
    public void testSymbolicLink() throws Exception {
        assertTrue(new FinderLocal("/tmp").isSymbolicLink());
        assertFalse(new FinderLocal("/private/tmp").isSymbolicLink());
        assertFalse(new FinderLocal("/t").isSymbolicLink());
    }

    @Test
    public void testGetSymlinkTarget() throws Exception {
        assertEquals(new FinderLocal("/private/tmp"), new FinderLocal("/tmp").getSymlinkTarget());
    }

    @Test
    public void testGetSymlinkTargetAbsolute() throws Exception {
        assertEquals(new FinderLocal("/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java"),
                new FinderLocal("/usr/bin/java").getSymlinkTarget());
    }
}
