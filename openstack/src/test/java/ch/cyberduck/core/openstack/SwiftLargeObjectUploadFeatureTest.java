package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.iterate.openstack.swift.model.StorageObject;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftLargeObjectUploadFeatureTest extends AbstractSwiftTest {

    @Test
    public void testAppendNoPartCompleted() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 2 * 1024 * 1024;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        try {
            new SwiftLargeObjectUploadFeature(session, new SwiftRegionService(session), new SwiftWriteFeature(session, new SwiftRegionService(session)),
                1 * 1024L * 1024L, 1).upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener() {
                long count;

                @Override
                public void sent(final long bytes) {
                    count += bytes;
                    if(count >= 32768L) {
                        throw new RuntimeException();
                    }
                }
            }, status, new DisabledLoginCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(0L, status.getOffset(), 0L);
        assertFalse(status.isComplete());

        final TransferStatus append = new TransferStatus().append(true).length(content.length);
        new SwiftLargeObjectUploadFeature(session, new SwiftRegionService(session), new SwiftWriteFeature(session, new SwiftRegionService(session)),
            1 * 1024L * 1024L, 1).upload(test, local,
            new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), append,
            new DisabledLoginCallback());
        assertTrue(new SwiftFindFeature(session).find(test));
        assertEquals(content.length, new SwiftAttributesFinderFeature(session).find(test).getSize());
        assertEquals(content.length, append.getOffset(), 0L);
        assertTrue(append.isComplete());
        final byte[] buffer = new byte[content.length];
        final InputStream in = new SwiftReadFeature(session, new SwiftRegionService(session)).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testAppendSecondPart() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final String name = UUID.randomUUID().toString();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 2 * 1024 * 1024;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        try {
            new SwiftLargeObjectUploadFeature(session, new SwiftRegionService(session), new SwiftWriteFeature(session, new SwiftRegionService(session)),
                1024L * 1024L, 1).upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener() {
                long count;

                @Override
                public void sent(final long bytes) {
                    count += bytes;
                    if(count >= 1.1 * 1024L * 1024L) {
                        throw new RuntimeException();
                    }
                }
            }, status, new DisabledLoginCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(1024L * 1024L, status.getOffset(), 0L);
        assertFalse(status.isComplete());

        final TransferStatus append = new TransferStatus().append(true).length(1024L * 1024L).skip(1024L * 1024L);
        new SwiftLargeObjectUploadFeature(session, new SwiftRegionService(session), new SwiftWriteFeature(session, new SwiftRegionService(session)),
            1024L * 1024L, 1).upload(test, local,
            new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), append,
            new DisabledLoginCallback());
        assertEquals(2 * 1024L * 1024L, append.getOffset(), 0L);
        assertTrue(append.isComplete());
        assertTrue(new SwiftFindFeature(session).find(test));
        assertEquals(2 * 1024L * 1024L, new SwiftAttributesFinderFeature(session).find(test).getSize(), 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new SwiftReadFeature(session, new SwiftRegionService(session)).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUpload() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());

        // Each segment, except the last, must be larger than 1048576 bytes.
        //2MB + 1
        final int length = 1048576 + 1048576 + 1;
        final byte[] content = RandomUtils.nextBytes(length);

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
        assertEquals(Checksum.NONE, Checksum.parse(object.getMd5sum()));
        assertEquals(Checksum.NONE, new SwiftAttributesFinderFeature(session).find(test).getChecksum());
        assertNotNull(new DefaultAttributesFinderFeature(session).find(test).getChecksum().hash);

        assertTrue(status.isComplete());
        // Verify not canceled
        status.validate();
        assertEquals(content.length, status.getOffset());

        assertTrue(new SwiftFindFeature(session).find(test));
        final InputStream in = new SwiftReadFeature(session, regionService).read(test, new TransferStatus(), new DisabledConnectionCallback());
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
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
