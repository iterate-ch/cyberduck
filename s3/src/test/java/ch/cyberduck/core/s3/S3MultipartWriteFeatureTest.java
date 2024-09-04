package ch.cyberduck.core.s3;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3MultipartWriteFeatureTest extends AbstractS3Test {

    @Test
    public void testWrite() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3MultipartWriteFeature feature = new S3MultipartWriteFeature(session, acl);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
        final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
        assertEquals(content.length, count.getSent());
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertEquals(content.length, out.getStatus().getContentLength());
        assertTrue(new S3FindFeature(session, acl).find(file));
        final PathAttributes attr = new S3AttributesFinderFeature(session, acl).find(file);
        assertEquals(status.getResponse().getChecksum(), attr.getChecksum());
        assertEquals(content.length, attr.getSize());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new S3ReadFeature(session).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteVirtualHost() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(virtualhost);
        final S3MultipartWriteFeature feature = new S3MultipartWriteFeature(virtualhost, acl);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
        final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
        assertEquals(content.length, count.getSent());
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertEquals(content.length, out.getStatus().getContentLength());
        assertTrue(new S3FindFeature(virtualhost, acl).find(file));
        final PathAttributes attr = new S3AttributesFinderFeature(virtualhost, acl).find(file);
        assertEquals(status.getResponse().getChecksum(), attr.getChecksum());
        assertEquals(content.length, attr.getSize());
        final byte[] compare = new byte[content.length];
        final InputStream stream = new S3ReadFeature(virtualhost).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new S3DefaultDeleteFeature(virtualhost, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteZeroLength() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3MultipartWriteFeature feature = new S3MultipartWriteFeature(session, acl);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final byte[] content = RandomUtils.nextBytes(0);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new S3ReadFeature(session).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteZeroLengthVersioning() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3MultipartWriteFeature feature = new S3MultipartWriteFeature(session, acl);
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final byte[] content = RandomUtils.nextBytes(0);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new S3ReadFeature(session).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
