package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.kms.KMSEncryptionFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.jets3t.service.model.S3Object;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3MultipartUploadServiceTest {

    @Test
    public void testUploadSinglePart() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3MultipartUploadService service = new S3MultipartUploadService(session, 5 * 1024L, 2);
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString() + ".txt";
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final String random = RandomStringUtils.random(1000);
        IOUtils.write(random, local.getOutputStream(false), Charset.defaultCharset());
        final TransferStatus status = new TransferStatus();
        status.setLength((long) random.getBytes().length);
        status.setMime("text/plain");
        status.setStorageClass(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertEquals((long) random.getBytes().length, status.getOffset(), 0L);
        assertTrue(status.isComplete());
        assertTrue(new S3FindFeature(session).find(test));
        final PathAttributes attributes = new S3AttributesFeature(session).find(test);
        assertEquals(random.getBytes().length, attributes.getSize());
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, new S3StorageClassFeature(session).getClass(test));
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                //
            }
        });
        local.delete();
        session.close();
    }

    @Test
    public void testUploadSinglePartEncrypted
            () throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3MultipartUploadService service = new S3MultipartUploadService(session, 5 * 1024L, 2);
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString() + ".txt";
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final String random = RandomStringUtils.random(1000);
        IOUtils.write(random, local.getOutputStream(false), Charset.defaultCharset());
        final TransferStatus status = new TransferStatus();
        status.setEncryption(KMSEncryptionFeature.SSE_KMS_DEFAULT);
        status.setLength((long) random.getBytes().length);
        status.setMime("text/plain");
        status.setStorageClass(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertEquals((long) random.getBytes().length, status.getOffset(), 0L);
        assertTrue(status.isComplete());
        assertTrue(new S3FindFeature(session).find(test));
        final PathAttributes attributes = new S3AttributesFeature(session).find(test);
        assertEquals(random.getBytes().length, attributes.getSize());
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, new S3StorageClassFeature(session).getClass(test));
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertEquals("aws:kms", metadata.get("server-side-encryption"));
        assertNotNull(metadata.get("server-side-encryption-aws-kms-key-id"));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                //
            }
        });
        local.delete();
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testUploadInvalidContainer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3MultipartUploadService m = new S3MultipartUploadService(session, 5 * 1024L, 1);
        final Path container = new Path("nosuchcontainer.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final TransferStatus status = new TransferStatus();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), status, null);
    }

    @Test
    public void testMultipleParts() throws Exception {
        // 5L * 1024L * 1024L
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3MultipartUploadService m = new S3MultipartUploadService(session, 5242880L, 5);
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] random = new byte[5242881];
        new Random().nextBytes(random);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), status, null);
        assertEquals((long) random.length, status.getOffset(), 0L);
        assertTrue(status.isComplete());
        assertTrue(new S3FindFeature(session).find(test));
        assertEquals(random.length, session.list(container,
                new DisabledListProgressListener()).get(test).attributes().getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                //
            }
        });
        local.delete();
        session.close();
    }

    @Test
    public void testMultiplePartsWithSHA256Checksum() throws Exception {
        // 5L * 1024L * 1024L
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        ))) {
        };
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3MultipartUploadService m = new S3MultipartUploadService(session, 5242880L, 5);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] random = new byte[5242881];
        new Random().nextBytes(random);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), status, null);
        assertEquals((long) random.length, status.getOffset(), 0L);
        assertTrue(status.isComplete());
        assertTrue(new S3FindFeature(session).find(test));
        assertEquals(random.length, session.list(container,
                new DisabledListProgressListener()).get(test).attributes().getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                //
            }
        });
        local.delete();
        session.close();
    }

    @Test
    public void testAppendSecondPart() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString();
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final byte[] random = new byte[12 * 1024 * 1024];
        new Random().nextBytes(random);
        Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        IOUtils.write(random, local.getOutputStream(false));
        final AtomicBoolean started = new AtomicBoolean();
        final TransferStatus status = new TransferStatus() {
            @Override
            public void progress(long bytes) {
                super.progress(bytes);
                started.set(true);
            }
        };
        status.setLength(random.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        try {
            new S3MultipartUploadService(session, 10L * 1024L * 1024L, 1).upload(test, new Local(System.getProperty("java.io.tmpdir"), name) {
                        @Override
                        public InputStream getInputStream() throws AccessDeniedException {
                            return new CountingInputStream(super.getInputStream()) {
                                @Override
                                protected void beforeRead(int n) throws IOException {
                                    if(started.get()) {
                                        if(this.getByteCount() >= 11L * 1024L * 1024L) {
                                            throw new IOException();
                                        }
                                    }
                                }
                            };
                        }
                    },
                    new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), status,
                    new DisabledLoginCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(10L * 1024L * 1024L, status.getOffset(), 0L);
        assertFalse(status.isComplete());
        assertFalse(new S3FindFeature(session).find(test));

        final TransferStatus append = new TransferStatus().append(true).length(random.length);
        new S3MultipartUploadService(session, 10L * 1024L * 1024L, 1).upload(test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), append,
                new DisabledConnectionCallback());
        assertEquals(2L * 1024L * 1024L, append.getOffset(), 0L);
        assertTrue(append.isComplete());
        assertTrue(new S3FindFeature(session).find(test));
        assertEquals(12L * 1024L * 1024L, new S3AttributesFeature(session).find(test).getSize(), 0L);
        final byte[] buffer = new byte[random.length];
        final InputStream in = new S3ReadFeature(session).read(test, new TransferStatus());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(random, buffer);
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
                //
            }
        });
        local.delete();
        session.close();
    }

    @Test
    public void testAppendNoPartCompleted() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        String name = UUID.randomUUID().toString();
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] random = new byte[32769];
        new Random().nextBytes(random);
        IOUtils.write(random, local.getOutputStream(false));
        final AtomicBoolean started = new AtomicBoolean();
        final TransferStatus status = new TransferStatus() {
            @Override
            public void progress(long bytes) {
                super.progress(bytes);
                started.set(true);
            }
        };
        status.setLength(random.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        try {
            new S3MultipartUploadService(session, 10485760L, 1).upload(test, new Local(System.getProperty("java.io.tmpdir"), name) {
                        @Override
                        public InputStream getInputStream() throws AccessDeniedException {
                            return new CountingInputStream(super.getInputStream()) {
                                @Override
                                protected void beforeRead(int n) throws IOException {
                                    if(started.get()) {
                                        if(this.getByteCount() >= 32768) {
                                            throw new IOException();
                                        }
                                    }
                                }
                            };
                        }
                    }, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), status,
                    new DisabledConnectionCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(0L, status.getOffset(), 0L);
        assertFalse(status.isComplete());

        final TransferStatus append = new TransferStatus().append(true).length(random.length);
        new S3MultipartUploadService(session, 10485760L, 1).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), append,
                new DisabledConnectionCallback());
        assertEquals(32769L, append.getOffset(), 0L);
        assertTrue(append.isComplete());
        assertTrue(new S3FindFeature(session).find(test));
        assertEquals(random.length, session.list(container,
                new DisabledListProgressListener()).get(test).attributes().getSize());
        final byte[] buffer = new byte[random.length];
        final InputStream in = new S3ReadFeature(session).read(test, new TransferStatus());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(random, buffer);
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
        session.close();
    }
}
