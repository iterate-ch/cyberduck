package ch.cyberduck.core.importer;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FireFtpBookmarkCollectionTest {

    @Test(expected = AccessDeniedException.class)
    public void testParseNotFound() throws Exception {
        new FireFtpBookmarkCollection().read(new Local(System.getProperty("java.io.tmpdir"), "f"));
    }

    @Test
    public void testParse() throws Exception {
        FireFtpBookmarkCollection c = new FireFtpBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new Local("src/test/resources/org.mozdev.fireftp"));
        assertEquals(1, c.size());
    }

    @Test
    public void testLoad() throws Exception {
        FireFtpBookmarkCollection c = new FireFtpBookmarkCollection() {
            @Override
            public Local getFile() {
                try {
                    return new Local("src/test/resources/org.mozdev.fireftp");
                }
                catch(LocalAccessDeniedException e) {
                    fail();
                    return null;
                }
            }
        };
        assertEquals(0, c.size());
        c.load();
        assertEquals(1, c.size());
    }
}
