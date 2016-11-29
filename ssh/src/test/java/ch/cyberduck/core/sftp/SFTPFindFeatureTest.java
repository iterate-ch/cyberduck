package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

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
    public void testListCryptomator() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        final PathCache cache = new PathCache(1);
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), cache);
        final Path home = new SFTPHomeDirectoryService(session).find();
        final Path vault = new Path(home, "/cryptomator-vault/test", EnumSet.of(Path.Type.directory));
        session.withVault(new CryptoVault(session, home, new DisabledPasswordStore(), new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("coke4you");
            }
        }).load());
        assertTrue(session.getFeature(Find.class).find(new Path(vault, "blabal", EnumSet.of(Path.Type.directory))));
        assertFalse(session.getFeature(Find.class).find(new Path(vault, "bla", EnumSet.of(Path.Type.directory))));
        session.close();
    }
}
