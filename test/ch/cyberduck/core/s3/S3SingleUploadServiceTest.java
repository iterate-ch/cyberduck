package ch.cyberduck.core.s3;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.gstorage.GoogleStorageSession;
import ch.cyberduck.core.io.AbstractStreamListener;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class S3SingleUploadServiceTest extends AbstractTestCase {

    @Test
    public void testUploadAmazon() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final S3SingleUploadService m = new S3SingleUploadService(session);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        test.setLocal(new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
        final String random = RandomStringUtils.random(1000);
        final OutputStream out = test.getLocal().getOutputStream(false);
        IOUtils.write(random, out);
        IOUtils.closeQuietly(out);
        final TransferStatus status = new TransferStatus();
        status.setLength(random.getBytes().length);
        m.upload(test, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new AbstractStreamListener(), status);
        assertTrue(session.exists(test));
        assertEquals(random.getBytes().length, session.list(container,
                new DisabledListProgressListener()).get(test.getReference()).attributes().getSize());
        session.delete(test, new DisabledLoginController());
        session.close();
    }

    @Test
    public void testUploadGoogle() throws Exception {
        final Host host = new Host(Protocol.GOOGLESTORAGE_SSL, Protocol.GOOGLESTORAGE_SSL.getDefaultHostname(), new Credentials(
                properties.getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                if(user.equals("Google OAuth2 Access Token")) {
                    return properties.getProperty("google.accesstoken");
                }
                if(user.equals("Google OAuth2 Refresh Token")) {
                    return properties.getProperty("google.refreshtoken");
                }
                return null;
            }
        }, new DisabledLoginController());
        final S3SingleUploadService m = new S3SingleUploadService(session);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        test.setLocal(new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
        final String random = RandomStringUtils.random(1000);
        final OutputStream out = test.getLocal().getOutputStream(false);
        IOUtils.write(random, out);
        IOUtils.closeQuietly(out);
        final TransferStatus status = new TransferStatus();
        status.setLength(random.getBytes().length);
        m.upload(test, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new AbstractStreamListener(), status);
        assertTrue(session.exists(test));
        assertEquals(random.getBytes().length, session.list(container,
                new DisabledListProgressListener()).get(test.getReference()).attributes().getSize());
        session.delete(test, new DisabledLoginController());
        session.close();
    }


    @Test(expected = NotfoundException.class)
    public void testUploadInvalidContainer() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final S3SingleUploadService m = new S3SingleUploadService(session);
        final Path container = new Path("nosuchcontainer.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        test.setLocal(new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
        test.getLocal().touch();
        final TransferStatus status = new TransferStatus();
        m.upload(test, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new AbstractStreamListener(), status);
    }
}
