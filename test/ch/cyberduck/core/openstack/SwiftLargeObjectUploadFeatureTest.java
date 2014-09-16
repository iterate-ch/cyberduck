package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftLargeObjectUploadFeatureTest extends AbstractTestCase {

    @Test
    public void testUploadHP() throws Exception {
        final Host host = new Host(new SwiftProtocol() {
            @Override
            public String getContext() {
                return "/v2.0/tokens";
            }
        }, "region-a.geo-1.identity.hpcloudsvc.com", 35357, new Credentials(
                properties.getProperty("hpcloud.key"), properties.getProperty("hpcloud.secret")
        ));
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("region-a.geo-1");
        this.test(host, container);
    }

    @Test(expected = NotfoundException.class)
    public void testUploadHPNotFound() throws Exception {
        final Host host = new Host(new SwiftProtocol() {
            @Override
            public String getContext() {
                return "/v2.0/tokens";
            }
        }, "region-a.geo-1.identity.hpcloudsvc.com", 35357, new Credentials(
                properties.getProperty("hpcloud.key"), properties.getProperty("hpcloud.secret")
        ));
        final Path container = new Path("t.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("region-b.geo-1");
        this.test(host, container);
    }

    @Test
    public void testUploadRax() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(
                        properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")));
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        this.test(host, container);
    }

    private void test(final Host host, final Path container) throws Exception {
        final SwiftSession session = new SwiftSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());

        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));

        final FinderLocal local = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());

        // Each segment, except the last, must be larger than 1048576 bytes.
        //2MB + 1
        final byte[] content = new byte[1048576 + 1048576 + 1];
        new Random().nextBytes(content);

        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);

        final SwiftLargeObjectUploadFeature upload = new SwiftLargeObjectUploadFeature(session,
                new SwiftObjectListService(session),
                new SwiftSegmentService(session, ".segments-test/"), new SwiftWriteFeature(session), (long) (content.length / 2), 4);

        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status, new DisabledLoginController());

        assertEquals(1048576 + 1048576 + 1, status.getCurrent());
        assertTrue(status.isComplete());
        assertFalse(status.isCanceled());

        assertTrue(new SwiftFindFeature(session).find(test));
        final InputStream in = new SwiftReadFeature(session).read(test, new TransferStatus());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        new StreamCopier(status, status).transfer(in, buffer);
        IOUtils.closeQuietly(in);
        assertArrayEquals(content, buffer.toByteArray());
        final Map<String, String> metadata = new SwiftMetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        final List<Path> segments = new SwiftSegmentService(session).list(test);
        assertFalse(segments.isEmpty());
        assertEquals(3, segments.size());
        assertEquals(1048576L, segments.get(0).attributes().getSize());
        assertEquals(1048576L, segments.get(1).attributes().getSize());
        assertEquals(1L, segments.get(2).attributes().getSize());
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController());
        session.close();
    }
}
