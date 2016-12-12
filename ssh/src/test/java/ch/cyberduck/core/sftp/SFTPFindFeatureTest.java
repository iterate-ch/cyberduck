package ch.cyberduck.core.sftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cryptomator.DisabledVaultLookupListener;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.pool.SingleSessionPool;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SFTPFindFeatureTest {

    @Test
    public void testFindNotFound() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertFalse(new SFTPFindFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
        session.close();
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertTrue(new SFTPFindFeature(session).find(new SFTPHomeDirectoryService(session).find()));
        assertTrue(new SFTPFindFeature(session).find(new Path(new SFTPHomeDirectoryService(session).find(), ".ssh", EnumSet.of(AbstractPath.Type.directory))));
        session.close();
    }

    @Test
    public void testFindFile() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertTrue(new SFTPFindFeature(session).find(new Path(new SFTPHomeDirectoryService(session).find(), ".bash_profile", EnumSet.of(AbstractPath.Type.file))));
        session.close();
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new SFTPFindFeature(new SFTPSession(new Host(new SFTPProtocol()))).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testFindCryptomator() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SingleSessionPool pool = new SingleSessionPool(new LoginConnectionService(
                new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()
        ), new SFTPSession(host), PathCache.empty(), new DisabledPasswordStore(), new DisabledPasswordCallback());
        final Session<?> session = pool.borrow(BackgroundActionState.running);
        final Path home = session.getFeature(Home.class).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore(), new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        }, new DisabledVaultLookupListener()).create(session, null);
        session.withVault(cryptomator);
        assertFalse(session.getFeature(Find.class).find(new Path(vault, "a", EnumSet.of(Path.Type.directory))));
        session.getFeature(Touch.class).touch(test, new TransferStatus());
        assertTrue(session.getFeature(Find.class).find(test));
        session.getFeature(Delete.class).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testFindLongFilenameCryptomator() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SingleSessionPool pool = new SingleSessionPool(new LoginConnectionService(
                new DisabledLoginCallback(), new DisabledHostKeyCallback(), new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener()
        ), new SFTPSession(host), PathCache.empty(), new DisabledPasswordStore(), new DisabledPasswordCallback());
        final Session<?> session = pool.borrow(BackgroundActionState.running);
        final Path home = session.getFeature(Home.class).find();
        final Path vault = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(vault, RandomStringUtils.random(130), EnumSet.of(Path.Type.file));
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore(), new DisabledPasswordCallback() {
            @Override
            public void prompt(final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("vault");
            }
        }, new DisabledVaultLookupListener()).create(session, null);
        session.withVault(cryptomator);
        session.getFeature(Touch.class).touch(test, new TransferStatus());
        assertTrue(session.getFeature(Find.class).find(test));
        session.getFeature(Delete.class).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
