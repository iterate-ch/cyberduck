package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.AbstractStreamListener;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class S3MultipartUploadServiceTest extends AbstractTestCase {

    @Test
    public void testUploadSinglePart() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final S3MultipartUploadService m = new S3MultipartUploadService(session, 5 * 1024L);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        test.setLocal(new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
        final String random = RandomStringUtils.random(1000);
        IOUtils.write(random, test.getLocal().getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.getBytes().length);
        m.upload(test, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new AbstractStreamListener(), status);
        assertTrue(session.exists(test));
        assertEquals(random.getBytes().length, session.list(container,
                new DisabledListProgressListener()).get(test.getReference()).attributes().getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test));
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
        final S3MultipartUploadService m = new S3MultipartUploadService(session, 5 * 1024L);
        final Path container = new Path("nosuchcontainer.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        final TransferStatus status = new TransferStatus();
        m.upload(test, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new AbstractStreamListener(), status);
    }

    @Test
    public void testMultipleParts() throws Exception {
        // 5L * 1024L * 1024L
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final S3MultipartUploadService m = new S3MultipartUploadService(session, 5 * 1024L);
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        test.setLocal(new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));
        final String random = RandomStringUtils.random(10 * 5 * 1024 * 1024);
        IOUtils.write(random, test.getLocal().getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(random.getBytes().length);
        m.upload(test, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new AbstractStreamListener(), status);
        assertTrue(session.exists(test));
        assertEquals(random.getBytes().length, session.list(container,
                new DisabledListProgressListener()).get(test.getReference()).attributes().getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test));
        session.close();
    }
}
