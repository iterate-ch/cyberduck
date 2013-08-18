package ch.cyberduck.core.serializer.impl;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.HostReaderFactory;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Serializable;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.local.FinderLocal;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @version $Id$
 */
public class HostPlistReaderTest extends AbstractTestCase {

    @Test
    public void testDeserializeDeprecatedProtocol() throws Exception {
        assertNull(HostReaderFactory.get().read(LocalFactory.createLocal("test")));
        assertEquals(new FTPProtocol(), new HostPlistReader().read(
                LocalFactory.createLocal("test/ch/cyberduck/core/serializer/impl/1c158c34-db8a-4c32-a732-abd9447bb27c.duck")).getProtocol());
    }

    @Test
    public void testRead() throws Exception {
        PlistReader reader = new HostPlistReader();
        final Serializable read = reader.read(new FinderLocal("bookmarks/s3.amazonaws.com – S3.duck"));
        assertNotNull(read);
    }

    @Test
    public void testReadNotFound() throws Exception {
        PlistReader reader = new HostPlistReader();
        final Serializable read = reader.read(new FinderLocal("notfound.duck"));
        assertNull(read);
    }
}
