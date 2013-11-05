package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftWriteFeatureTest extends AbstractTestCase {

    @Test
    public void testWrite() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final TransferStatus status = new TransferStatus();
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", Path.FILE_TYPE);
        test.attributes().setRegion("DFW");
        final OutputStream out = new SwiftWriteFeature(session).write(test, status);
        assertNotNull(out);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        assertTrue(new SwiftFindFeature(session).find(test));
        final PathAttributes attributes = session.list(test.getParent(), new DisabledListProgressListener()).get(test.getReference()).attributes();
        assertEquals(content.length, attributes.getSize());
        assertEquals(0L, new SwiftWriteFeature(session).append(test, status.getLength(), Cache.empty()).size, 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new SwiftReadFeature(session).read(test, new TransferStatus());
        IOUtils.readFully(in, buffer);
        IOUtils.closeQuietly(in);
        assertArrayEquals(content, buffer);
        final Map<String, String> metadata = new SwiftMetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController());
        session.close();
    }

    @Test
    public void testAppendBelowLimit() throws Exception {
        assertFalse(new SwiftWriteFeature(null).append(new Path("/p", Path.FILE_TYPE), 0L, Cache.empty()).append);
        assertFalse(new SwiftWriteFeature(null).append(new Path("/p", Path.FILE_TYPE), 2L * 1024L * 1024L * 1024L - 32768 - 1, Cache.empty()).append);
    }

    @Test
    public void testAppendNoSegmentFound() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        assertFalse(new SwiftWriteFeature(session, new SwiftObjectListService(session) {
            @Override
            public AttributedList<Path> list(Path directory, ListProgressListener listener) throws BackgroundException {
                return new AttributedList<Path>(Collections.<Path>emptyList());
            }
        }, new SwiftSegmentService(session)).append(new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE), 2L * 1024L * 1024L * 1024L - 32768, Cache.empty()).append);
    }

    @Test
    public void testAppend() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final Path file = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        final SwiftSegmentService segments = new SwiftSegmentService(session, ".test");
        final Write.Append append = new SwiftWriteFeature(session, new SwiftObjectListService(session) {
            @Override
            public AttributedList<Path> list(Path directory, ListProgressListener listener) throws BackgroundException {
                final Path segment1 = new Path(container, segments.name(file, 0L, 1), Path.FILE_TYPE);
                segment1.attributes().setSize(1L);
                final Path segment2 = new Path(container, segments.name(file, 0L, 2), Path.FILE_TYPE);
                segment2.attributes().setSize(2L);
                return new AttributedList<Path>(Arrays.asList(segment1, segment2));
            }
        }, segments).append(file, 2L * 1024L * 1024L * 1024L - 32768, Cache.empty());
        assertTrue(append.append);
        assertEquals(3L, append.size, 0L);
    }
}
