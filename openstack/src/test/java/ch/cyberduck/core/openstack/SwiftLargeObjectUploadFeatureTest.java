package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
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
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.iterate.openstack.swift.model.StorageObject;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftLargeObjectUploadFeatureTest extends AbstractSwiftTest {

    @Test
    public void testAppendNoPartCompleted() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final String name = new AlphanumericRandomStringService().random();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 2 * 1024 * 1024;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        final BytecountStreamListener listener = new BytecountStreamListener();
        try {
            new SwiftLargeObjectUploadFeature(session, new SwiftRegionService(session), new SwiftWriteFeature(session, new SwiftRegionService(session)),
                1 * 1024L * 1024L, 1).upload(test, new Local(System.getProperty("java.io.tmpdir"), name) {
                @Override
                public InputStream getInputStream() throws AccessDeniedException {
                    return new CountingInputStream(super.getInputStream()) {
                        @Override
                        protected void beforeRead(int n) throws IOException {
                            if(listener.getSent() >= 32768L) {
                                throw new IOException();
                            }
                        }
                    };
                }
            }, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), listener, status, new DisabledLoginCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(32768L, listener.getSent());
        assertFalse(status.isComplete());
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getResponse().getSize());

        final TransferStatus append = new TransferStatus().append(true).withLength(content.length);
        new SwiftLargeObjectUploadFeature(session, new SwiftRegionService(session), new SwiftWriteFeature(session, new SwiftRegionService(session)),
                1 * 1024L * 1024L, 1).upload(test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), append,
                new DisabledLoginCallback());
        assertEquals(content.length, append.getResponse().getSize());
        assertTrue(new SwiftFindFeature(session).find(test));
        assertEquals(content.length, new SwiftAttributesFinderFeature(session).find(test).getSize());
        assertTrue(append.isComplete());
        assertNotSame(PathAttributes.EMPTY, append.getResponse());
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
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final String name = new AlphanumericRandomStringService().random();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 2 * 1024 * 1024;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final SwiftLargeObjectUploadFeature feature = new SwiftLargeObjectUploadFeature(session, regionService, new SwiftWriteFeature(session, regionService),
            1024L * 1024L, 1);
        final BytecountStreamListener listener = new BytecountStreamListener();
        try {
            feature.upload(test, new Local(System.getProperty("java.io.tmpdir"), name) {
                @Override
                public InputStream getInputStream() throws AccessDeniedException {
                    return new CountingInputStream(super.getInputStream()) {
                        @Override
                        protected void beforeRead(int n) throws IOException {
                            if(this.getByteCount() >= 1.1 * 1024L * 1024L) {
                                throw new IOException();
                            }
                        }
                    };
                }
            }, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), listener, status, new DisabledLoginCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(1024L * 1024L, listener.getSent());
        assertFalse(status.isComplete());
        assertEquals(0, new SwiftSegmentService(session, regionService).list(test).size());
        assertTrue(feature.append(test, status).append);
        assertTrue(new SwiftFindFeature(session).find(test));
        assertEquals(1 * 1024L * 1024L, new SwiftAttributesFinderFeature(session).find(test).getSize());

        final TransferStatus append = new TransferStatus().append(true).withLength(1024L * 1024L).withOffset(1024L * 1024L);
        feature.upload(test, local,
            new BandwidthThrottle(BandwidthThrottle.UNLIMITED), listener, append,
            new DisabledLoginCallback());
        assertEquals(2 * 1024L * 1024L, listener.getSent());
        assertTrue(append.isComplete());
        assertNotSame(PathAttributes.EMPTY, append.getResponse());
        assertEquals(content.length, append.getResponse().getSize());
        assertTrue(new SwiftFindFeature(session).find(test));
        assertEquals(2 * 1024L * 1024L, new SwiftAttributesFinderFeature(session).find(test).getSize());
        assertEquals(2, new SwiftSegmentService(session, regionService).list(test).size());
        final byte[] buffer = new byte[content.length];
        final InputStream in = new SwiftReadFeature(session, regionService).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUpload() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, new AlphanumericRandomStringService().random() + ".txt", EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());

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

        final BytecountStreamListener count = new BytecountStreamListener();
        final StorageObject object = upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count,
                status, new DisabledConnectionCallback());
        assertEquals(Checksum.NONE, Checksum.parse(object.getMd5sum()));
        assertNotEquals(Checksum.NONE, new SwiftAttributesFinderFeature(session).find(test).getChecksum());
        assertNotNull(new DefaultAttributesFinderFeature(session).find(test).getChecksum().hash);

        assertTrue(status.isComplete());
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertEquals(content.length, status.getResponse().getSize());

        // Verify not canceled
        status.validate();
        assertEquals(content.length, count.getSent());

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
        assertEquals(0, new SwiftSegmentService(session).list(test).size());
        local.delete();
    }
}
