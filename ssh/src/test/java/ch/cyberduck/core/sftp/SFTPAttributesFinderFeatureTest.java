package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPAttributesFinderFeatureTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        new SFTPAttributesFinderFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SFTPAttributesFinderFeature f = new SFTPAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(new SFTPHomeDirectoryService(session).find());
        assertNotNull(attributes);
        // Test wrong type
        try {
            f.find(new Path(new SFTPHomeDirectoryService(session).find().getAbsolute(), EnumSet.of(Path.Type.file)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        session.close();
    }

    @Test
    public void testAttributesDirectoryListingAccessDenied() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
                throw new AccessDeniedException("f");
            }
        };
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SFTPAttributesFinderFeature f = new SFTPAttributesFinderFeature(session);
        final Path file = new Path(new SFTPHomeDirectoryService(session).find(), "dropbox/f", EnumSet.of(Path.Type.file));
        final Attributes attributes = f.find(file);
        assertEquals(37L, attributes.getSize());
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
        session.getFeature(Vault.class).load(vault, new DisabledPasswordStore(), new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                credentials.setPassword("coke4you");
            }
        });
        final PathAttributes attributes = session.getFeature(ch.cyberduck.core.features.Attributes.class).find(
                new Path(vault, "blabal", EnumSet.of(Path.Type.directory)));
        assertNotNull(attributes);
        session.close();
    }
}
