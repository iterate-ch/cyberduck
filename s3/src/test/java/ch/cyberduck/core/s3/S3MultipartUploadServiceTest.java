package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jets3t.service.model.S3Object;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
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
        final S3MultipartUploadService m = new S3MultipartUploadService(session, 5 * 1024L, 2);
        m.withStorage(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString() + ".txt";
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final String random = RandomStringUtils.random(1000);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength((long) random.getBytes().length);
        status.setMime("text/plain");
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
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
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
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
        final Path container = new Path("test.eu-central-1.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
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
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] random = new byte[10485760];
        new Random().nextBytes(random);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        try {
            new S3MultipartUploadService(session, 10485760L, 1).upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener() {
                long count;

                @Override
                public void sent(final long bytes) {
                    count += bytes;
                    if(count >= 5242880) {
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
        assertEquals(5242880L, status.getOffset(), 0L);
        assertFalse(status.isComplete());
        assertFalse(new S3FindFeature(session).find(test));

        final TransferStatus append = new TransferStatus().append(true).length(random.length);
        new S3MultipartUploadService(session, 10485760L, 1).upload(test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), append,
                new DisabledLoginCallback());
        assertEquals(random.length, append.getOffset(), 0L);
        assertTrue(append.isComplete());
        assertTrue(new S3FindFeature(session).find(test));
        assertEquals(random.length, session.list(container,
                new DisabledListProgressListener()).get(test).attributes().getSize());
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
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] random = new byte[32769];
        new Random().nextBytes(random);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        try {
            new S3MultipartUploadService(session, 10485760L, 1).upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener() {
                long count;

                @Override
                public void sent(final long bytes) {
                    count += bytes;
                    if(count >= 32768) {
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
        assertEquals(32768L, status.getOffset(), 0L);
        assertFalse(status.isComplete());

        final TransferStatus append = new TransferStatus().append(true).length(random.length);
        new S3MultipartUploadService(session, 10485760L, 1).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), append,
                new DisabledLoginCallback());
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
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }
}
