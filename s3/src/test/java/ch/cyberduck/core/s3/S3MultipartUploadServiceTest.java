package ch.cyberduck.core.s3;

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.kms.KMSEncryptionFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.S3Object;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3MultipartUploadServiceTest extends AbstractS3Test {

    @Test
    public void testUploadSinglePart() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3MultipartUploadService service = new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl, 5 * 1024L * 1024L, 2);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = String.format(" %s.txt", UUID.randomUUID());
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] random = RandomUtils.nextBytes(1021);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        status.setMime("text/plain");
        final long ts = 1684090690000L;
        status.setTimestamp(ts);
        status.setStorageClass(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        final BytecountStreamListener count = new BytecountStreamListener();
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                count, status, new DisabledLoginCallback());
        assertEquals(random.length, count.getSent());
        assertSame(Checksum.NONE, status.getResponse().getChecksum());
        assertTrue(status.isComplete());
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertEquals(random.length, status.getResponse().getSize());
        assertEquals(ts, status.getResponse().getModificationDate());
        assertTrue(new S3FindFeature(session, acl).find(test));
        final PathAttributes attr = new S3AttributesFinderFeature(session, acl).find(test);
        assertEquals(status.getResponse().getETag(), attr.getETag());
        assertEquals(status.getResponse().getChecksum(), attr.getChecksum());
        assertEquals(random.length, attr.getSize());
        assertEquals(Checksum.NONE, attr.getChecksum());
        assertEquals(ts, attr.getModificationDate());
        assertNotNull(attr.getETag());
        // d2b77e21aa68ebdcbfb589124b9f9192-1
        assertEquals(Checksum.NONE, Checksum.parse(attr.getETag()));
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, new S3StorageClassFeature(session, acl).getClass(test));
        final Map<String, String> metadata = new S3MetadataFeature(session, acl).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadSinglePartEncrypted() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3MultipartUploadService service = new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl, 5 * 1024L * 1024L, 2);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID() + ".txt";
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] random = RandomUtils.nextBytes(1023);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setEncryption(KMSEncryptionFeature.SSE_KMS_DEFAULT);
        status.setLength(random.length);
        status.setMime("text/plain");
        status.setTimestamp(System.currentTimeMillis());
        status.setStorageClass(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        final BytecountStreamListener count = new BytecountStreamListener();
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                count, status, new DisabledLoginCallback());
        assertEquals(random.length, count.getSent());
        assertSame(Checksum.NONE, status.getResponse().getChecksum());
        assertEquals(random.length, status.getResponse().getSize());
        assertTrue(status.isComplete());
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertTrue(new S3FindFeature(session, acl).find(test));
        final PathAttributes attr = new S3AttributesFinderFeature(session, acl).find(test);
        assertEquals(status.getResponse().getETag(), attr.getETag());
        assertEquals(status.getResponse().getChecksum(), attr.getChecksum());
        assertEquals(random.length, attr.getSize());
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, new S3StorageClassFeature(session, acl).getClass(test));
        final Map<String, String> metadata = new S3MetadataFeature(session, acl).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertEquals("aws:kms", metadata.get("server-side-encryption"));
        assertNotNull(metadata.get("server-side-encryption-aws-kms-key-id"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test(expected = NotfoundException.class)
    public void testUploadInvalidContainer() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3MultipartUploadService m = new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl, 5 * 1024L * 1024L, 1);
        final Path container = new Path("nosuchcontainer.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TransferStatus status = new TransferStatus();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), status, null);
    }

    @Test
    public void testMultipleParts() throws Exception {
        // 5L * 1024L * 1024L
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3MultipartUploadService m = new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl, 5 * 1024L * 1024L, 5);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 5242881;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final BytecountStreamListener count = new BytecountStreamListener();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, null);
        assertEquals(content.length, count.getSent());
        assertTrue(status.isComplete());
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertEquals(content.length, new S3AttributesFinderFeature(session, acl).find(test).getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testMultiplePartsWithSHA256Checksum() throws Exception {
        // 5L * 1024L * 1024L
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3MultipartUploadService m = new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl, 5 * 1024L * 1024L, 5);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final int length = 5242881;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setTimestamp(System.currentTimeMillis());
        final BytecountStreamListener count = new BytecountStreamListener();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, null);
        assertEquals(content.length, count.getSent());
        assertTrue(status.isComplete());
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertEquals(content.length, status.getResponse().getSize());
        assertSame(Checksum.NONE, status.getResponse().getChecksum());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertEquals(content.length, new S3AttributesFinderFeature(session, acl).find(test).getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testAppendSecondPart() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString();
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final int length = 12 * 1024 * 1024;
        final byte[] content = RandomUtils.nextBytes(length);
        Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setTimestamp(System.currentTimeMillis());
        final AtomicBoolean interrupt = new AtomicBoolean();
        final BytecountStreamListener count = new BytecountStreamListener();
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        try {
            new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl, 10L * 1024L * 1024L, 1).upload(test, new Local(System.getProperty("java.io.tmpdir"), name) {
                        @Override
                        public InputStream getInputStream() throws AccessDeniedException {
                            return new CountingInputStream(super.getInputStream()) {
                                @Override
                                protected void beforeRead(int n) throws IOException {
                                    if(this.getByteCount() >= 11L * 1024L * 1024L) {
                                        throw new IOException();
                                    }
                                }
                            };
                    }
                },
                    new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status,
                    new DisabledLoginCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(10L * 1024L * 1024L, count.getSent());
        assertFalse(status.isComplete());
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getResponse().getSize());
        final Path upload = new S3ListService(session, acl).list(container, new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertTrue(new S3FindFeature(session, acl).find(upload));
        assertNotNull(upload);
        assertTrue(upload.getType().contains(Path.Type.upload));
        assertTrue(new S3FindFeature(session, acl).find(upload));
        assertEquals(10L * 1024L * 1024L, new S3AttributesFinderFeature(session, acl).find(upload).getSize());
        final TransferStatus append = new TransferStatus().append(true).withLength(2L * 1024L * 1024L).withOffset(10L * 1024L * 1024L);
        new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl, 10L * 1024L * 1024L, 1).upload(test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, append,
                new DisabledConnectionCallback());
        assertEquals(12L * 1024L * 1024L, count.getSent());
        assertTrue(append.isComplete());
        assertNotSame(PathAttributes.EMPTY, append.getResponse());
        assertEquals(content.length, append.getResponse().getSize());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertEquals(12L * 1024L * 1024L, new S3AttributesFinderFeature(session, acl).find(test).getSize());
        final byte[] buffer = new byte[content.length];
        final InputStream in = new S3ReadFeature(session).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testAppendNoPartCompleted() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        String name = UUID.randomUUID().toString();
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 32769;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        final BytecountStreamListener count = new BytecountStreamListener();
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        try {
            new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl, 10485760L, 1).upload(test, new Local(System.getProperty("java.io.tmpdir"), name) {
                        @Override
                        public InputStream getInputStream() throws AccessDeniedException {
                            return new CountingInputStream(super.getInputStream()) {
                                @Override
                                protected void beforeRead(int n) throws IOException {
                                    if(this.getByteCount() >= 32768) {
                                        throw new IOException();
                                    }
                                }
                            };
                    }
                }, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status,
                new DisabledConnectionCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(0L, count.getSent());
        assertEquals(0L, status.getOffset());
        assertFalse(status.isComplete());

        final TransferStatus append = new TransferStatus().append(true).withLength(content.length);
        new S3MultipartUploadService(session, new S3WriteFeature(session, acl), acl, 10485760L, 1).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                count, append,
                new DisabledConnectionCallback());
        assertEquals(32769L, count.getSent());
        assertTrue(append.isComplete());
        assertEquals(content.length, append.getResponse().getSize());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertEquals(content.length, new S3AttributesFinderFeature(session, acl).find(test).getSize());
        final byte[] buffer = new byte[content.length];
        final InputStream in = new S3ReadFeature(session).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
