package ch.cyberduck.core.ftp;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class FTPWriteFeatureTest {

    @Test
    public void testReadWrite() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final OutputStream out = new FTPWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize());
        assertEquals(content.length, new FTPWriteFeature(session).append(test, status.getLength(), PathCache.empty()).size, 0L);
        {
            final InputStream in = new FTPReadFeature(session).read(test, new TransferStatus().length(content.length), new DisabledConnectionCallback());
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            new StreamCopier(status, status).transfer(in, buffer);
            in.close();
            assertArrayEquals(content, buffer.toByteArray());
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new FTPReadFeature(session).read(test, new TransferStatus().length(content.length).append(true).skip(1L), new DisabledConnectionCallback());
            IOUtils.readFully(in, buffer);
            in.close();
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testWriteContentRange() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final FTPWriteFeature feature = new FTPWriteFeature(session);
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(64000);
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(1024L);
            final OutputStream out = feature.write(test, status, new DisabledConnectionCallback());
            // Write first 1024
            new StreamCopier(status, status).withOffset(0L).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(1024L, new DefaultAttributesFinderFeature(session).find(test).getSize());
        {
            // Remaining chunked transfer with offset
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length - 1024L);
            status.setOffset(1024L);
            status.setAppend(true);
            final OutputStream out = feature.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        IOUtils.copy(new FTPReadFeature(session).read(test, new TransferStatus().length(content.length), new DisabledConnectionCallback()), out);
        assertArrayEquals(content, out.toByteArray());
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    @Ignore
    public void testWriteRangeEndFirst() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final FTPWriteFeature feature = new FTPWriteFeature(session);
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2048);
        {
            // Write end of file first
            final TransferStatus status = new TransferStatus();
            status.setLength(1024L);
            status.setOffset(1024L);
            status.setAppend(true);
            final OutputStream out = feature.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(test).getSize());
        {
            // Write beginning of file up to the last chunk
            final TransferStatus status = new TransferStatus();
            status.setExists(true);
            status.setOffset(0L);
            status.setLength(1024L);
            status.setAppend(true);
            final OutputStream out = feature.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.close();
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        IOUtils.copy(new FTPReadFeature(session).read(test, new TransferStatus().length(content.length), new DisabledConnectionCallback()), out);
        assertArrayEquals(content, out.toByteArray());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new DefaultAttributesFinderFeature(session).find(test).getSize());
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }


    @Test(expected = AccessDeniedException.class)
    public void testWriteNotFound() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new FTPWorkdirService(session).find().getAbsolute() + "/nosuchdirectory/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new FTPWriteFeature(session).write(test, new TransferStatus(), new DisabledConnectionCallback());
    }

    @Test
    public void testAppend() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertEquals(false, new FTPWriteFeature(session).append(
                new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 0L, PathCache.empty()).append);
        final Path f = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(f, new TransferStatus());
        assertEquals(true, new FTPWriteFeature(session).append(f, 0L, PathCache.empty()).append);
        new FTPDeleteFeature(session).delete(Collections.singletonList(f), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
