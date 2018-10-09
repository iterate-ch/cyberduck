package ch.cyberduck.core.sftp;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
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
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        new SFTPAttributesFinderFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
            System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final SFTPAttributesFinderFeature f = new SFTPAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(new SFTPHomeDirectoryService(session).find(), new DisabledListProgressListener());
        assertNotNull(attributes);
        // Test wrong type
        try {
            f.find(new Path(new SFTPHomeDirectoryService(session).find().getAbsolute(), EnumSet.of(Path.Type.file)), new DisabledListProgressListener());
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        session.close();
    }

    @Test
    public void testFindSymbolicLink() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
            System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path file = new SFTPTouchFeature(session).touch(new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path symlink = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPSymlinkFeature(session).symlink(symlink, file.getAbsolute());
        final SFTPAttributesFinderFeature f = new SFTPAttributesFinderFeature(session);
        final PathAttributes attributes = f.find(symlink, new DisabledListProgressListener());
        assertNotNull(attributes);
        session.close();
    }

    @Test
    public void testAttributesDirectoryListingAccessDenied() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
            System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == ListService.class) {
                    return (T) new SFTPListService(this) {
                        @Override
                        public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
                            throw new AccessDeniedException("f");
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path folder = new Path(new SFTPHomeDirectoryService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SFTPDirectoryFeature(session).mkdir(folder, null, new TransferStatus());
        new SFTPTouchFeature(session).touch(file, new TransferStatus());
        new SFTPUnixPermissionFeature(session).setUnixPermission(folder, new Permission("-wx------"));
        assertEquals(new Permission("-wx------"), new SFTPUnixPermissionFeature(session).getUnixPermission(folder));
        final Attributes attributes = new SFTPAttributesFinderFeature(session).find(file, new DisabledListProgressListener());
        assertEquals(0L, attributes.getSize());
        new SFTPDeleteFeature(session).delete(Arrays.asList(file, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
