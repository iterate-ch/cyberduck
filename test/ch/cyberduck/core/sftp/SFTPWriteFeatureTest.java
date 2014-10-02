package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

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

/**
 * @version $Id$
 */
public class SFTPWriteFeatureTest extends AbstractTestCase {

    @Test
    public void testWrite() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final TransferStatus status = new TransferStatus();
        final byte[] content = new byte[1048576];
        new Random().nextBytes(content);
        status.setLength(content.length);
        final Path test = new Path(session.workdir(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final OutputStream out = new SFTPWriteFeature(session).write(test, status);
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertTrue(new SFTPFindFeature(session).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test.getReference()).attributes().getSize());
        assertEquals(content.length, new SFTPWriteFeature(session).append(test, status.getLength(), Cache.<Path>empty()).size, 0L);
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
            final InputStream in = new SFTPReadFeature(session).read(test, new TransferStatus().length(content.length));
            new StreamCopier(status, status).transfer(in, buffer);
            assertArrayEquals(content, buffer.toByteArray());
        }
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 1);
            final InputStream in = new SFTPReadFeature(session).read(test, new TransferStatus().append(true).current(1L));
            new StreamCopier(status, status).transfer(in, buffer);
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer.toByteArray());
        }
        new SFTPDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController(), new DisabledProgressListener());
        session.close();
    }

    @Test
    public void testWriteSymlink() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path target = new Path(session.workdir(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPTouchFeature(session).touch(target);
        assertTrue(new SFTPFindFeature(session).find(target));
        final String name = UUID.randomUUID().toString();
        final Path symlink = new Path(session.workdir(), name, EnumSet.of(Path.Type.file, AbstractPath.Type.symboliclink));
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
            IOUtils.closeQuietly(in);
            assertArrayEquals(new byte[0], buffer);
        }
        final AttributedList<Path> list = new SFTPListService(session).list(session.workdir(), new DisabledListProgressListener());
        assertTrue(list.contains(new Path(session.workdir(), name, EnumSet.of(Path.Type.file))));
        assertFalse(list.contains(symlink));
        new SFTPDeleteFeature(session).delete(Arrays.asList(target, symlink), new DisabledLoginController(), new DisabledProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testWriteNotFound() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path test = new Path(new DefaultHomeFinderService(session).find().getAbsolute() + "/nosuchdirectory/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SFTPWriteFeature(session).write(test, new TransferStatus());
    }

    @Test
    public void testAppend() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        final Path test = new Path(session.workdir(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertEquals(false, new SFTPWriteFeature(session).append(
                new Path(session.workdir(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 0L, Cache.<Path>empty()).append);
        assertEquals(true, new SFTPWriteFeature(session).append(
                new Path(session.workdir(), "test", EnumSet.of(Path.Type.file)), 0L, Cache.<Path>empty()).append);
    }
}
