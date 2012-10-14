package ch.cyberduck.ui.cocoa.serializer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.local.FinderLocal;

import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class PlistReaderTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        HostPlistReader.register();
    }

    @Test
    public void testRead() throws Exception {
        PlistReader reader = new HostPlistReader();
        final Serializable read = reader.read(new FinderLocal("bookmarks/mirror.switch.ch – FTP.duck"));
        assertNotNull(read);
    }
}
