package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultAttributesFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import ch.iterate.openstack.swift.model.StorageObject;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftLargeObjectUploadFeatureTest {

    @Test
    public void testUploadRax() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                new Credentials(
                        System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")));
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final SwiftSession session = new SwiftSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());

        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));

        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());

        // Each segment, except the last, must be larger than 1048576 bytes.
        //2MB + 1
        final byte[] content = new byte[1048576 + 1048576 + 1];
        new Random().nextBytes(content);

        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);

        final SwiftRegionService regionService = new SwiftRegionService(session);
        final SwiftLargeObjectUploadFeature upload = new SwiftLargeObjectUploadFeature(session,
                regionService,
                new SwiftObjectListService(session, regionService),
                new SwiftSegmentService(session, ".segments-test/"),
                new SwiftWriteFeature(session, regionService), (long) (content.length / 2), 4);

        final StorageObject object = upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status, new DisabledConnectionCallback());
        assertNull(Checksum.parse(object.getMd5sum()));
        assertNull(new SwiftAttributesFeature(session).find(test).getChecksum());
        assertNotNull(new DefaultAttributesFeature(session).find(test).getChecksum());

        assertTrue(status.isComplete());
        assertFalse(status.isCanceled());
        assertEquals(content.length, status.getOffset());

        assertTrue(new SwiftFindFeature(session).find(test));
        final InputStream in = new SwiftReadFeature(session, regionService).read(test, new TransferStatus());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        new StreamCopier(status, status).transfer(in, buffer);
        in.close();
        buffer.close();
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
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }
}
