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
        assertFalse(n.exists());
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
        assertFalse(l.exists());
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
