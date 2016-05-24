package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
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

/**
 * @version $Id:$
 */
@Category(IntegrationTest.class)
public class S3MultipartWriteFeatureTest {

    @Test
    public void testWriteUploadLargeBuffer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3MultipartWriteFeature feature = new S3MultipartWriteFeature(session);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final ResponseOutputStream<List<MultipartPart>> out = feature.write(file, status);
        final byte[] content = RandomStringUtils.random(6 * 1024 * 1024).getBytes("UTF-8");
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        // Adjust buffer to be 5MB. This will write parts with 5MB size which is the minimum allowed
        final byte[] buffer = new byte[5 * 1024 * 1024];
        assertEquals(content.length, IOUtils.copyLarge(in, out, buffer));
        in.close();
        out.close();
        assertNotNull(out.getResponse());
        assertTrue(new S3FindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new S3ReadFeature(session).read(file, new TransferStatus().length(content.length));
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                //
            }
        });
        session.close();
    }

    @Test
    public void testWriteUploadSmallBuffer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3MultipartWriteFeature feature = new S3MultipartWriteFeature(session);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final ResponseOutputStream<List<MultipartPart>> out = feature.write(file, status);
        final byte[] content = RandomStringUtils.random(5 * 1024 * 1024).getBytes("UTF-8");
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        // Adjust buffer to be 5MB. This will write parts with 5MB size which is the minimum allowed
        final byte[] buffer = new byte[1 * 1024 * 1024];
        assertEquals(content.length, IOUtils.copyLarge(in, out, buffer));
        in.close();
        out.close();
        assertNotNull(out.getResponse());
        assertTrue(new S3FindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new S3ReadFeature(session).read(file, new TransferStatus().length(content.length));
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                //
            }
        });
        session.close();
    }
}