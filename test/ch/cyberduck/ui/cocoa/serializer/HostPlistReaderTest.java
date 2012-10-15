package ch.cyberduck.ui.cocoa.serializer;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class HostPlistReaderTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        HostPlistReader.register();
    }

    @Test
    public void testDeserializeDeprecatedProtocol() throws Exception {
        assertEquals(Protocol.FTP, new HostPlistReader().deserialize(NSDictionary.dictionaryWithContentsOfFile(
                "test/ch/cyberduck/ui/cocoa/serializer/1c158c34-db8a-4c32-a732-abd9447bb27c.duck"
        )).getProtocol());
    }

    @Test
    public void testRead() throws Exception {
        PlistReader reader = new HostPlistReader();
        final Serializable read = reader.read(new FinderLocal("bookmarks/mirror.switch.ch – FTP.duck"));
        assertNotNull(read);
    }
}
