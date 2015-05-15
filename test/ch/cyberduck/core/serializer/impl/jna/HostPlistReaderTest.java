package ch.cyberduck.core.serializer.impl.jna;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.test.Depends;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class HostPlistReaderTest extends AbstractTestCase {

    @Test(expected = LocalAccessDeniedException.class)
    public void testDeserializeNoSuchFile() throws Exception {
        final HostPlistReader reader = new HostPlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        assertNull(reader.read(new Local(UUID.randomUUID().toString())));
    }

    @Test
    public void testDeserializeDeprecatedProtocol() throws Exception {
        final HostPlistReader reader = new HostPlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        assertNull(reader.read(
                new FinderLocal("test/ch/cyberduck/core/serializer/impl/1c158c34-db8a-4c32-a732-abd9447bb27c.duck")));
    }

    @Test
    public void testRead() throws Exception {
        final HostPlistReader reader = new HostPlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Host read = reader.read(new FinderLocal(
                "test/ch/cyberduck/core/serializer/impl/s3.amazonaws.com – S3.duck"));
        assertNotNull(read);
        assertEquals("Amazon Simple Storage Service & CloudFront CDN", read.getComment());
        assertEquals(ProtocolFactory.S3_SSL, read.getProtocol());
    }

    @Test
    public void testReadPrivateKey() throws Exception {
        final HostPlistReader reader = new HostPlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Host read = reader.read(new FinderLocal(
                "test/ch/cyberduck/core/serializer/impl/Private Key Legacy.duck"));
        assertNotNull(read);
        assertEquals(ProtocolFactory.SFTP, read.getProtocol());
        assertNotNull(read.getCredentials().getIdentity());
        assertEquals("~/.ssh/key.pem", read.getCredentials().getIdentity().getAbbreviatedPath());
    }

    @Test
    public void testReadPrivateKeyBookmark() throws Exception {
        final HostPlistReader reader = new HostPlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        final Host read = reader.read(new FinderLocal(
                "test/ch/cyberduck/core/serializer/impl/Private Key.duck"));
        assertNotNull(read);
        assertEquals(ProtocolFactory.SFTP, read.getProtocol());
        assertNotNull(read.getCredentials().getIdentity());
        assertEquals("~/.ssh/key.pem", read.getCredentials().getIdentity().getAbbreviatedPath());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testReadNotFound() throws Exception {
        final HostPlistReader reader = new HostPlistReader(new DeserializerFactory(PlistDeserializer.class.getName()));
        reader.read(new FinderLocal("notfound.duck"));
    }
}
