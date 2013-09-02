package ch.cyberduck.core.s3;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.gstorage.GoogleStorageProtocol;
import ch.cyberduck.core.gstorage.GoogleStorageSession;
import ch.cyberduck.core.io.AbstractStreamListener;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.Collections;
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
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
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
        Preferences.instance().setProperty("s3.storage.class", "REDUCED_REDUNDANCY");
        m.upload(test, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new AbstractStreamListener(), status);
        assertTrue(new S3FindFeature(session).find(test));
        final PathAttributes attributes = session.list(container,
                new DisabledListProgressListener()).get(test.getReference()).attributes();
        assertEquals(random.getBytes().length, attributes.getSize());
        assertEquals("REDUCED_REDUNDANCY", attributes.getStorageClass());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController());
        session.close();
    }

    @Test
    public void testUploadGoogle() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
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
        assertTrue(new S3FindFeature(session).find(test));
        final PathAttributes attributes = session.list(container,
                new DisabledListProgressListener()).get(test.getReference()).attributes();
        assertEquals(random.getBytes().length, attributes.getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController());
        session.close();
    }


    @Test(expected = NotfoundException.class)
    public void testUploadInvalidContainer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
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
