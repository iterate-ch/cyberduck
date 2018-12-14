package ch.cyberduck.core.s3;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.MultipartPart;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3MultipartWriteFeatureTest extends AbstractS3Test {

    @Test
    public void testWrite() throws Exception {
        final S3MultipartWriteFeature feature = new S3MultipartWriteFeature(session);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<List<MultipartPart>> out = feature.write(file, status, new DisabledConnectionCallback());
        final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        new StreamCopier(new TransferStatus(), progress).transfer(in, out);
        assertEquals(content.length, progress.getOffset());
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertTrue(new S3FindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new S3ReadFeature(session).read(file, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testWriteZeroLength() throws Exception {
        final S3MultipartWriteFeature feature = new S3MultipartWriteFeature(session);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final byte[] content = RandomUtils.nextBytes(0);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<List<MultipartPart>> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new S3ReadFeature(session).read(file, new TransferStatus().length(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
