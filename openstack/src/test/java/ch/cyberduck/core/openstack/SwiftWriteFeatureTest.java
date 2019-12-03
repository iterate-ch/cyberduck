package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftWriteFeatureTest extends AbstractSwiftTest {

    @Test
    public void testWrite() throws Exception {
        final TransferStatus status = new TransferStatus();
        status.setMime("text/plain");
        final byte[] content = "test".getBytes(StandardCharsets.UTF_8);
        status.setLength(content.length);
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        final SwiftRegionService regionService = new SwiftRegionService(session);
        status.setMetadata(Collections.singletonMap("C", "duck"));
        final OutputStream out = new SwiftWriteFeature(session, regionService).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertTrue(new SwiftFindFeature(session).find(test));
        final PathAttributes attributes = new SwiftListService(session, regionService).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes();
        assertEquals(content.length, attributes.getSize());
        final Write.Append append = new SwiftWriteFeature(session, regionService).append(test, status.getLength(), PathCache.empty());
        assertTrue(append.override);
        assertEquals(content.length, append.size, 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new SwiftReadFeature(session, regionService).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        final Map<String, String> metadata = new SwiftMetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertEquals("duck", metadata.get("X-Object-Meta-C"));
        final OutputStream overwrite = new SwiftWriteFeature(session, regionService).write(test, new TransferStatus().length(0L), new DisabledConnectionCallback());
        overwrite.close();
        new SwiftDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testAppendNoSegmentFound() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final AtomicBoolean list = new AtomicBoolean();
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final Write.Append append = new SwiftWriteFeature(session, regionService, new SwiftObjectListService(session, regionService) {
            @Override
            public AttributedList<Path> list(Path directory, ListProgressListener listener) {
                list.set(true);
                return new AttributedList<Path>(Collections.<Path>emptyList());
            }
        }, new SwiftSegmentService(session)).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 2L * 1024L * 1024L * 1024L, PathCache.empty());
        assertTrue(list.get());
        assertFalse(append.append);
        assertFalse(append.override);
        assertEquals(Write.notfound, append);
    }

    @Test
    public void testAppendSegmentFound() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SwiftSegmentService segments = new SwiftSegmentService(session, ".test");
        final AtomicBoolean list = new AtomicBoolean();
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final Write.Append append = new SwiftWriteFeature(session, regionService, new SwiftObjectListService(session, regionService) {
            @Override
            public AttributedList<Path> list(Path directory, ListProgressListener listener) {
                list.set(true);
                final Path segment1 = segments.getSegment(file, 0L, 1);
                segment1.attributes().setSize(1L);
                final Path segment2 = segments.getSegment(file, 0L, 2);
                segment2.attributes().setSize(2L);
                return new AttributedList<Path>(Arrays.asList(segment1, segment2));
            }
        }, segments).append(file, 2L * 1024L * 1024L * 1024L, PathCache.empty());
        assertTrue(append.append);
        assertEquals(3L, append.size, 0L);
        assertTrue(list.get());
    }

    @Test
    public void testOverride() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final AtomicBoolean list = new AtomicBoolean();
        final AtomicBoolean find = new AtomicBoolean();
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final Write.Append append = new SwiftWriteFeature(session, regionService, new SwiftObjectListService(session, regionService) {
            @Override
            public AttributedList<Path> list(Path directory, ListProgressListener listener) {
                list.set(true);
                return new AttributedList<Path>(Collections.singletonList(file));
            }
        }, new SwiftSegmentService(session), new Find() {
            @Override
            public boolean find(final Path file) {
                find.set(true);
                return true;
            }
        }, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file) {
                return new PathAttributes();
            }
        }
        ).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 1024L, PathCache.empty());
        assertFalse(append.append);
        assertTrue(append.override);
        assertFalse(list.get());
        assertTrue(find.get());
    }

    @Test
    public void testNotFound() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final AtomicBoolean list = new AtomicBoolean();
        final AtomicBoolean find = new AtomicBoolean();
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final Write.Append append = new SwiftWriteFeature(session, regionService, new SwiftObjectListService(session, regionService) {
            @Override
            public AttributedList<Path> list(Path directory, ListProgressListener listener) {
                list.set(true);
                return new AttributedList<Path>(Collections.singletonList(file));
            }
        }, new SwiftSegmentService(session), new Find() {
            @Override
            public boolean find(final Path file) {
                find.set(true);
                return false;
            }
        }
        ).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 1024L, PathCache.empty());
        assertFalse(append.append);
        assertFalse(append.override);
        assertEquals(Write.notfound, append);
        assertFalse(list.get());
        assertTrue(find.get());
    }
}
