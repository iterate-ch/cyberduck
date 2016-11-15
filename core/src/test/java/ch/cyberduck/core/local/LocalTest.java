package ch.cyberduck.core.local;

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class LocalTest {

    @Test
    public void testList() throws Exception {
        assertFalse(new Local("../profiles").list().isEmpty());
        assertTrue(new Local("../profiles").list(new Filter<String>() {
            @Override
            public boolean accept(final String file) {
                return false;
            }
        }).isEmpty());
    }

    @Test(expected = AccessDeniedException.class)
    public void testReadNoFile() throws Exception {
        final String name = UUID.randomUUID().toString();
        TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir") + "/" + name);
        l.getInputStream();
    }

    @Test
    public void testEqual() throws Exception {
        assertEquals(new TestLocal("/p/1"), new TestLocal("/p/1"));
        assertNotEquals(new TestLocal("/p/1"), new TestLocal("/p/2"));
        assertEquals(new TestLocal("/p/1"), new TestLocal("/P/1"));
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(new TestLocal("/p/1").hashCode(), new TestLocal("/P/1").hashCode());
    }

    @Test
    public void testAttributes() throws Exception {
        final TestLocal l = new TestLocal("/p/1");
        assertNotNull(l.attributes());
        assertSame(l.attributes(), l.attributes());
    }

    @Test
    public void testDelimiter() throws Exception {
        Local l = new WindowsLocal("G:\\");
        assertEquals("G:\\", l.getAbsolute());
        assertEquals("", l.getName());

        l = new WindowsLocal("C:\\path\\relative");
        assertEquals("relative", l.getName());
        assertEquals("C:\\path\\relative", l.getAbsolute());

        l = new WindowsLocal("C:\\path", "cyberduck.log", "\\");
        assertEquals("cyberduck.log", l.getName());
        assertEquals("C:\\path\\cyberduck.log", l.getAbsolute());

        l = new WindowsLocal("C:\\path", "Sessions", "\\");
        assertEquals("Sessions", l.getName());
        assertEquals("C:\\path\\Sessions", l.getAbsolute());
    }

    @Test
    public void testIsChild() throws Exception {
        TestLocal l1 = new TestLocal("/");
        TestLocal l2 = new TestLocal("/");
        assertFalse(l1.isChild(l2));
        assertFalse(l2.isChild(l1));

        l1 = new TestLocal("/p/1");
        l2 = new TestLocal("/p/1");
        assertFalse(l1.isChild(l2));
        assertFalse(l2.isChild(l1));

        l1 = new TestLocal("/p/1");
        l2 = new TestLocal("/p/2");
        assertFalse(l1.isChild(l2));
        assertFalse(l2.isChild(l1));

        l1 = new TestLocal("/");
        l2 = new TestLocal("/p");
        assertFalse(l1.isChild(l2));
        assertTrue(l2.isChild(l1));

        l1 = new TestLocal("/");
        l2 = new TestLocal("/p/1");
        assertFalse(l1.isChild(l2));
        assertTrue(l2.isChild(l1));

        l1 = new TestLocal("/p/1");
        l2 = new TestLocal("/p/1/2");
        assertFalse(l1.isChild(l2));
        assertTrue(l2.isChild(l1));

        WindowsLocal wl1 = new WindowsLocal("G:\\");
        WindowsLocal wl2 = new WindowsLocal("G:\\");
        assertFalse(wl1.isChild(wl2));
        assertFalse(wl2.isChild(wl1));

        wl1 = new WindowsLocal("G:\\");
        wl2 = new WindowsLocal("G:\\p");
        assertFalse(wl1.isChild(wl2));
        assertTrue(wl2.isChild(wl1));

        wl1 = new WindowsLocal("G:\\");
        wl2 = new WindowsLocal("H:\\p");
        assertFalse(wl1.isChild(wl2));
        assertFalse(wl2.isChild(wl1));

        wl1 = new WindowsLocal("G:\\");
        wl2 = new WindowsLocal("G:\\p\\1");
        assertFalse(wl1.isChild(wl2));
        assertTrue(wl2.isChild(wl1));

        wl1 = new WindowsLocal("G:\\p");
        wl2 = new WindowsLocal("G:\\p\\1\\2");
        assertFalse(wl1.isChild(wl2));
        assertTrue(wl2.isChild(wl1));
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testRenameExistingDirectory() throws Exception {
        final TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TestLocal n = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        l.mkdir();
        n.rename(l);
    }

    @Test
    public void testRenameDirectory() throws Exception {
        final TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TestLocal n = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        n.mkdir();
        n.rename(l);
        assertTrue(n.exists());
        assertTrue(l.exists());
        l.delete();
        assertFalse(l.exists());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final TestLocal l = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TestLocal n = new TestLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        new DefaultLocalTouchFeature().touch(n);
        l.rename(n);
        assertTrue(n.exists());
        assertTrue(l.exists());
        n.delete();
    }

    @Test
    public void testNormalize() throws Exception {
        assertEquals(StringUtils.removeEnd(System.getProperty("java.io.tmpdir"), "/"), new Local(System.getProperty("java.io.tmpdir")).getAbsolute());
    }

    private static class WindowsLocal extends Local {

        public WindowsLocal(final String parent, final String name, final String delimiter) throws LocalAccessDeniedException {
            super(parent, name, delimiter);
        }

        public WindowsLocal(final String name) throws LocalAccessDeniedException {
            super(name);
        }

        @Override
        public char getDelimiter() {
            return '\\';
        }
    }

    private final class TestLocal extends Local {
        private TestLocal(final String name) throws LocalAccessDeniedException {
            super(name);
        }

        public TestLocal(final String parent, final String name) throws LocalAccessDeniedException {
            super(parent, name);
        }
    }
}
