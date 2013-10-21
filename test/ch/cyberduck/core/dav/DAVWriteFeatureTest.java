package ch.cyberduck.core.dav;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultAttributesFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DAVWriteFeatureTest extends AbstractTestCase {

    @Test
    public void testReadWrite() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final TransferStatus status = new TransferStatus();
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        final OutputStream out = new DAVWriteFeature(session).write(test, status);
        assertNotNull(out);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test.getReference()).attributes().getSize(), 0L);
        assertEquals(content.length, new DAVWriteFeature(session).append(test, new DefaultAttributesFeature(session)).size, 0L);
        {
            final byte[] buffer = new byte[content.length];
            IOUtils.readFully(new DAVReadFeature(session).read(test, new TransferStatus()), buffer);
            assertArrayEquals(content, buffer);
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DAVReadFeature(session).read(test, new TransferStatus().length(content.length).append(true).current(1L));
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController());
        session.close();
    }

    @Test
    @Ignore
    public void testReadWritePixi() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "pulangyuta.pixi.me", new Credentials(
                "webdav", "webdav"
        ));
        host.setDefaultPath("/w/webdav/");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final TransferStatus status = new TransferStatus();
        final byte[] content = "test".getBytes("UTF-8");
        status.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        final OutputStream out = new DAVWriteFeature(session).write(test, status);
        assertNotNull(out);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(content.length, session.list(test.getParent(), new DisabledListProgressListener()).get(test.getReference()).attributes().getSize(), 0L);
        assertEquals(content.length, new DAVWriteFeature(session, false).append(test, new DefaultAttributesFeature(session)).size, 0L);
        {
            final byte[] buffer = new byte[content.length];
            IOUtils.readFully(new DAVReadFeature(session).read(test, new TransferStatus()), buffer);
            assertArrayEquals(content, buffer);
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DAVReadFeature(session).read(test, new TransferStatus().length(content.length).append(true).current(1L));
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController());
        session.close();
    }

    @Test(expected = AccessDeniedException.class)
    public void testWriteNotFound() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(new DefaultHomeFinderService(session).find().getAbsolute() + "/nosuchdirectory/" + UUID.randomUUID().toString(), Path.FILE_TYPE);
        new DAVWriteFeature(session).write(test, new TransferStatus());
    }

    @Test
    public void testAppend() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        assertEquals(false, new DAVWriteFeature(session).append(
                new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), Path.FILE_TYPE), new DefaultAttributesFeature(session)).append);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        new DefaultTouchFeature(session).touch(test);
        assertEquals(true, new DAVWriteFeature(session).append(test, new DefaultAttributesFeature(session)).append);
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController());
    }
}
