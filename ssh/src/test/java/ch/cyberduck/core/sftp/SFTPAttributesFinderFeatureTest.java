package ch.cyberduck.core.sftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.cryptomator.DisabledVaultLookupListener;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.pool.SingleSessionPool;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
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
        session.getFeature(Touch.class).touch(test, new TransferStatus());
        final PathAttributes attributes = session.getFeature(AttributesFinder.class).find(test);
        assertNotNull(attributes);
        session.getFeature(Delete.class).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
