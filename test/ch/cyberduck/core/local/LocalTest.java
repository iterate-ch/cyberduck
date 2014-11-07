package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class LocalTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        assertFalse(new Local("profiles") {

        }.list().isEmpty());
    }

    private final class TestLocal extends Local {
        private TestLocal(final String name) {
            super(name);
        }
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
    public void testSymbolicLink() throws Exception {
        assertTrue(new Local("/tmp").isSymbolicLink());
        assertFalse(new Local("/private/tmp").isSymbolicLink());
        assertFalse(new Local("/t").isSymbolicLink());
    }

    @Test
    public void testGetSymlinkTarget() throws Exception {
        assertEquals(new Local("/private/tmp"), new Local("/tmp").getSymlinkTarget());
    }
}
