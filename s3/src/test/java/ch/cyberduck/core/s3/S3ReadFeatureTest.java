package ch.cyberduck.core.s3;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3ReadFeatureTest extends AbstractS3Test {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3ReadFeature(session).read(new Path(container, "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
    }

    @Test
    public void testReadRange() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(1000);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new S3ReadFeature(session).read(test, status.withLength(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRangeUnknownLength() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(1000);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        status.setAppend(true);
        status.setOffset(100L);
        status.setLength(-1L);
        final InputStream in = new S3ReadFeature(session).read(test, status, new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadGzipContentEncoding() throws Exception {
        final ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
        final byte[] rawContent = RandomUtils.nextBytes(1457);
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressedStream);
        gzipOutputStream.write(rawContent);
        // Make sure to write
        gzipOutputStream.close();
        final byte[] compressedContent = compressedStream.toByteArray();
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().withLength(compressedContent.length);
        status.setMetadata(Collections.singletonMap(HttpHeaders.CONTENT_ENCODING, "gzip"));
        assertNotEquals(TransferStatus.UNKNOWN_LENGTH, status.getLength());
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(compressedContent), status));
        final OutputStream out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(compressedContent), out);
        final InputStream in = new S3ReadFeature(session).read(file, status, new DisabledConnectionCallback());
        assertNotNull(in);
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getLength());
        final BytecountStreamListener count = new BytecountStreamListener();
        final ByteArrayOutputStream received = new ByteArrayOutputStream();
        new StreamCopier(status, status).withListener(count).transfer(in, received);
        assertEquals(rawContent.length, count.getRecv());
        assertArrayEquals(rawContent, received.toByteArray());
        in.close();
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadCloseReleaseEntity() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final int length = 2048;
        final byte[] content = RandomUtils.nextBytes(length);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        final CountingInputStream in = new CountingInputStream(new S3ReadFeature(session).read(file, status, new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCloudFront() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(container, name, EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2018);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setAcl(Acl.CANNED_PUBLIC_READ);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new S3WriteFeature(session, new S3AccessControlListFeature(session)).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        final CountingInputStream in = new CountingInputStream(new S3ReadFeature(cloudfront).read(
                new Path(file.getName(), EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback()));
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(status, status).withListener(count).transfer(in, NullOutputStream.NULL_OUTPUT_STREAM);
        assertEquals(content.length, count.getRecv());
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testVirtualHostStyle() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(name, EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2018);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new S3WriteFeature(virtualhost, new S3AccessControlListFeature(session)).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        final CountingInputStream in = new CountingInputStream(new S3ReadFeature(virtualhost).read(
                new Path(file.getName(), EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback()));
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(status, status).withListener(count).transfer(in, NullOutputStream.NULL_OUTPUT_STREAM);
        assertEquals(content.length, count.getRecv());
        new S3DefaultDeleteFeature(virtualhost, new S3AccessControlListFeature(session)).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
