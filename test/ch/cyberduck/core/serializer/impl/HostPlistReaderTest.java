package ch.cyberduck.core.serializer.impl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.serializer.HostReaderFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @version $Id$
 */
public class HostPlistReaderTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        HostPlistReader.register();
    }

    @Test
    public void testDeserializeDeprecatedProtocol() throws Exception {
        assertNull(HostReaderFactory.get().read(LocalFactory.createLocal("test")));
        assertEquals(Protocol.FTP, HostReaderFactory.get().read(
                LocalFactory.createLocal("test/ch/cyberduck/core/serializer/impl/1c158c34-db8a-4c32-a732-abd9447bb27c.duck")).getProtocol());
    }

    @Test
    public void testRead() throws Exception {
        PlistReader reader = new HostPlistReader();
        final Serializable read = reader.read(new FinderLocal("bookmarks/mirror.switch.ch – FTP.duck"));
        assertNotNull(read);
    }
}
