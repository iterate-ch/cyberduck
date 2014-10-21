package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.gstorage.GoogleStorageProtocol;
import ch.cyberduck.core.gstorage.GoogleStorageSession;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3SingleUploadServiceTest extends AbstractTestCase {

    @Test
    public void testDecorate() throws Exception {
        final NullInputStream n = new NullInputStream(1L);
        assertSame(NullInputStream.class, new S3SingleUploadService(new S3Session(new Host("h"))).decorate(n, null).getClass());
    }

    @Test
    public void testDigest() throws Exception {
        assertNotNull(new S3SingleUploadService(new S3Session(new Host("h"))).digest());
    }

    @Test
    public void testUploadAmazon() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final S3WriteFeature write = new S3WriteFeature(session).withStorage("REDUCED_REDUNDANCY");
        final S3SingleUploadService service = new S3SingleUploadService(write);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID().toString() + ".txt";
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final FinderLocal local = new FinderLocal(System.getProperty("java.io.tmpdir"), name);
        final String random = RandomStringUtils.random(1000);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out);
        IOUtils.closeQuietly(out);
        final TransferStatus status = new TransferStatus();
        status.setLength(random.getBytes().length);
        status.setMime("text/plain");
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginController());
        assertTrue(new S3FindFeature(session).find(test));
        final PathAttributes attributes = new S3AttributesFeature(session).find(test);
        assertEquals(random.getBytes().length, attributes.getSize());
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController(), new DisabledProgressListener());
        session.close();
    }

    @Test
    public void testUploadGoogle() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                properties.getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
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
        }, new DisabledLoginController(), new DisabledCancelCallback());
        final S3SingleUploadService m = new S3SingleUploadService(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final FinderLocal local = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final String random = RandomStringUtils.random(1000);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out);
        IOUtils.closeQuietly(out);
        final TransferStatus status = new TransferStatus();
        status.setLength(random.getBytes().length);
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginController());
        assertTrue(new S3FindFeature(session).find(test));
        final PathAttributes attributes = session.list(container,
                new DisabledListProgressListener()).get(test.getReference()).attributes();
        assertEquals(random.getBytes().length, attributes.getSize());
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginController(), new DisabledProgressListener());
        session.close();
    }


    @Test(expected = NotfoundException.class)
    public void testUploadInvalidContainer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final S3SingleUploadService m = new S3SingleUploadService(session);
        final Path container = new Path("nosuchcontainer.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final FinderLocal local = new FinderLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        LocalTouchFactory.get().touch(local);
        final TransferStatus status = new TransferStatus();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status, new DisabledLoginController());
    }
}
