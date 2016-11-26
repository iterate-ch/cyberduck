package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.shared.DefaultAttributesFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPWriteFeatureTest {

    @Test
    public void testWriteThrottled() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        final byte[] content = new byte[1048576];
        new Random().nextBytes(content);
        status.setLength(content.length);
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final OutputStream out = new ThrottledOutputStream(new SFTPWriteFeature(session).write(test, status),
                new BandwidthThrottle(102400f));
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertTrue(new SFTPFindFeature(session).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize());
        assertEquals(content.length, new SFTPWriteFeature(session).append(test, status.getLength(), PathCache.empty()).size, 0L);
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new SFTPReadFeature(session).read(test, new TransferStatus().length(content.length));
            new StreamCopier(status, status).transfer(in, buffer);
            assertArrayEquals(content, buffer.toByteArray());
        }
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 1);
            final InputStream in = new SFTPReadFeature(session).read(test, new TransferStatus().append(true).skip(1L));
            new StreamCopier(status, status).transfer(in, buffer);
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer.toByteArray());
        }
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testWriteSymlink() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new SFTPHomeDirectoryService(session).find();
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(target);
        assertTrue(new SFTPFindFeature(session).find(target));
        final String name = UUID.randomUUID().toString();
        final Path symlink = new Path(workdir, name, EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
        new SFTPSymlinkFeature(session).symlink(symlink, target.getName());
        assertTrue(new SFTPFindFeature(session).find(symlink));
        final TransferStatus status = new TransferStatus();
        final byte[] content = new byte[1048576];
        new Random().nextBytes(content);
        status.setLength(content.length);
        status.setExists(true);
        final OutputStream out = new SFTPWriteFeature(session).write(symlink, status);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new SFTPReadFeature(session).read(symlink, new TransferStatus().length(content.length));
            new StreamCopier(status, status).transfer(in, buffer);
            assertArrayEquals(content, buffer.toByteArray());
        }
        {
            final byte[] buffer = new byte[0];
            final InputStream in = new SFTPReadFeature(session).read(target, new TransferStatus());
            IOUtils.readFully(in, buffer);
            in.close();
            assertArrayEquals(new byte[0], buffer);
        }
        final AttributedList<Path> list = new SFTPListService(session).list(workdir, new DisabledListProgressListener());
        assertTrue(list.contains(new Path(workdir, name, EnumSet.of(Path.Type.file))));
        assertFalse(list.contains(symlink));
        new SFTPDeleteFeature(session).delete(Arrays.asList(target, symlink), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path test = new Path(new SFTPHomeDirectoryService(session).find().getAbsolute() + "/nosuchdirectory/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPWriteFeature(session).write(test, new TransferStatus());
    }

    @Test
    public void testAppend() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path workdir = new SFTPHomeDirectoryService(session).find();
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertEquals(false, new SFTPWriteFeature(session).append(
                new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 0L, PathCache.empty()).append);
        assertEquals(true, new SFTPWriteFeature(session).append(
                new Path(workdir, "test", EnumSet.of(Path.Type.file)), 0L, PathCache.empty()).append);
    }

    @Test
    public void testWriteContentRange() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SFTPWriteFeature feature = new SFTPWriteFeature(session);
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(64000);
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(1024L);
            status.setOffset(0L);
            final OutputStream out = feature.write(test, status);
            // Write first 1024
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.flush();
            out.close();
        }
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(1024L, new DefaultAttributesFeature(session).find(test).getSize());
        {
            // Remaining chunked transfer with offset
            final TransferStatus status = new TransferStatus().exists(true);
            status.setLength(content.length - 1024L);
            status.setOffset(1024L);
            status.setAppend(true);
            final OutputStream out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.flush();
            out.close();
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        IOUtils.copy(new SFTPReadFeature(session).read(test, new TransferStatus().length(content.length)), out);
        assertArrayEquals(content, out.toByteArray());
        new SFTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteRangeEndFirst() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SFTPWriteFeature feature = new SFTPWriteFeature(session);
        final Path test = new Path(new SFTPHomeDirectoryService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(2048);
        {
            // Write end of file first
            final TransferStatus status = new TransferStatus();
            status.setLength(1024L);
            status.setOffset(1024L);
            status.setAppend(true);
            final OutputStream out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.flush();
            out.close();
        }
        assertEquals(2048, new DefaultAttributesFeature(session).find(test).getSize());
        {
            // Write beginning of file up to the last chunk
            final TransferStatus status = new TransferStatus().exists(true);
            status.setExists(true);
            status.setOffset(0L);
            status.setLength(1024L);
            status.setAppend(true);
            final OutputStream out = feature.write(test, status);
            new StreamCopier(status, status).withOffset(status.getOffset()).withLimit(status.getLength()).transfer(new ByteArrayInputStream(content), out);
            out.flush();
            out.close();
        }
        assertEquals(2048, new DefaultAttributesFeature(session).find(test).getSize());
        final ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        IOUtils.copy(new SFTPReadFeature(session).read(test, new TransferStatus().length(content.length)), out);
        assertArrayEquals(content, out.toByteArray());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertEquals(content.length, new DefaultAttributesFeature(session).find(test).getSize());
        new SFTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }


    @Test
    public void testUnconfirmedReadsNumber() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        final SFTPWriteFeature feature = new SFTPWriteFeature(session);
        assertEquals(33, feature.getMaxUnconfirmedWrites(new TransferStatus().length(TransferStatus.MEGA * 1L)));
        assertEquals(64, feature.getMaxUnconfirmedWrites(new TransferStatus().length((long) (TransferStatus.GIGA * 1.3))));
    }
}
