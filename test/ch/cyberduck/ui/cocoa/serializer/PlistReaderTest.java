package ch.cyberduck.ui.cocoa.serializer;

import ch.cyberduck.core.FinderLocal;
import ch.cyberduck.core.Serializable;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class PlistReaderTest {

    @Test
    public void testRead() throws Exception {
        PlistReader reader = new HostPlistReader();
        final Serializable read = reader.read(new FinderLocal("bookmarks/mirror.switch.ch – FTP.duck"));
        assertNotNull(read);
    }
}
