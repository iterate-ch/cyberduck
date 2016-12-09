package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.pool.SingleSessionPool;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3TouchFeatureTest {

    @Test
    public void testFile() {
        final S3Session session = new S3Session(new Host(new S3Protocol(), "h"));
        assertFalse(new S3TouchFeature(session, new S3WriteFeature(session)).isSupported(new Path("/", EnumSet.of(Path.Type.volume))));
        assertTrue(new S3TouchFeature(session, new S3WriteFeature(session)).isSupported(new Path(new Path("/", EnumSet.of(Path.Type.volume)), "/container", EnumSet.of(Path.Type.volume))));
    }

    @Test
    public void testTouch() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3WriteFeature(session)).touch(test);
        assertTrue(new S3FindFeature(session).find(test));
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }

    @Test(expected = AccessDeniedException.class)
    public void testFailureWithServerSideEncryptionBucketPolicy() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("sse-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3TouchFeature touch = new S3TouchFeature(session, new S3WriteFeature(session));
        final TransferStatus status = new TransferStatus();
        status.setEncryption(Encryption.Algorithm.NONE);
        touch.touch(test, status);
    }

    @Test
    public void testSuccessWithServerSideEncryptionBucketPolicy() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("sse-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3TouchFeature touch = new S3TouchFeature(session, new S3WriteFeature(session));
        final TransferStatus status = new TransferStatus();
        status.setEncryption(S3EncryptionFeature.SSE_AES256);
        status.setChecksum(ChecksumComputeFactory.get(HashAlgorithm.sha256)
                .compute(new NullInputStream(0L)));
        touch.touch(test, status);
    }

    @Test
    public void testConnectionReuse() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final S3TouchFeature service = new S3TouchFeature(session, new S3WriteFeature(session));
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final List<Path> list = new ArrayList<Path>();
        for(int i = 0; i < 200; i++) {
            final String name = String.format("%s-%d", UUID.randomUUID().toString(), i);
            final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
            service.touch(test);
            list.add(test);
        }
        new S3MultipleDeleteFeature(session).delete(list, new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testTouchEncrypted() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials(
                        System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                ));
        final SingleSessionPool pool = new SingleSessionPool(new LoginConnectionService(
                new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()
        ), new S3Session(host), PathCache.empty());
        final Session<?> session = pool.borrow(BackgroundActionState.running);
        final Path home = session.getFeature(Home.class).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore(), new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        }).create(session, null);
        session.withVault(cryptomator);
        session.getFeature(Touch.class).touch(test);
        assertTrue(session.getFeature(Find.class).find(test));
        session.getFeature(Delete.class).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testTouchLongFilenameEncrypted() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                new Credentials(
                        System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                ));
        final SingleSessionPool pool = new SingleSessionPool(new LoginConnectionService(
                new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()
        ), new S3Session(host), PathCache.empty());
        final Session<?> session = pool.borrow(BackgroundActionState.running);
        final Path home = session.getFeature(Home.class).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, RandomStringUtils.random(130), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore(), new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        }).create(session, null);
        session.withVault(cryptomator);
        session.getFeature(Touch.class).touch(test);
        assertTrue(session.getFeature(Find.class).find(test));
        session.getFeature(Delete.class).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
