package ch.cyberduck.core.dav;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DAVReadFeatureTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final TransferStatus status = new TransferStatus();
        new DAVReadFeature(session).read(new Path(session.workdir(), "nosuchname", EnumSet.of(Path.Type.file)), status);
    }

    @Test
    public void testReadChunkedTransfer() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener());
        service.connect(session, Cache.<Path>empty());
        final Path test = new Path("/trunk/LICENSE.txt", EnumSet.of(Path.Type.file));
        // Unknown length in status
        final TransferStatus status = new TransferStatus() {
            @Override
            public void setLength(long length) {
                assertEquals(923L, length);
                // Ignore update. As with unknown length for chunked transfer
            }
        };
        final FinderLocal local = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertEquals(-1L, local.attributes().getSize());
        new DefaultDownloadFeature(session).download(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertEquals(923L, local.attributes().getSize());
    }

    @Test
    public void testReadRange() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path test = new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature(session).touch(test);

        final FinderLocal local = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        new HttpUploadFeature(new DAVWriteFeature(session)).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                new TransferStatus().length(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setCurrent(100L);
        final Path workdir = session.workdir();
        final InputStream in = new DAVReadFeature(session).read(test, status);
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }
}
