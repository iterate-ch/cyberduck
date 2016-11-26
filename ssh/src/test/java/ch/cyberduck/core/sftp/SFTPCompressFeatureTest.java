package ch.cyberduck.core.sftp;

import ch.cyberduck.core.Archive;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPCompressFeatureTest {

    @Test
    public void testArchive() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final SFTPCompressFeature feature = new SFTPCompressFeature(session);
        for(Archive archive : Archive.getKnownArchives()) {
            final Path workdir = new SFTPHomeDirectoryService(session).find();
            final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
            session.getFeature(Touch.class).touch(test);
            feature.archive(archive, workdir, Collections.singletonList(test), new ProgressListener() {
                @Override
                public void message(final String message) {
                    //
                }
            }, new DisabledTranscriptListener());
            assertTrue(new SFTPFindFeature(session).find(archive.getArchive(Collections.singletonList(test))));
            new SFTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(),
                    new Delete.DisabledCallback());
            assertFalse(new SFTPFindFeature(session).find(test));
            feature.unarchive(archive, archive.getArchive(Collections.singletonList(test)), new ProgressListener() {
                @Override
                public void message(final String message) {
                    //
                }
            }, new DisabledTranscriptListener());
            assertTrue(new SFTPFindFeature(session).find(test));
            new SFTPDeleteFeature(session).delete(Collections.singletonList(archive.getArchive(
                    Collections.singletonList(test)
            )), new DisabledLoginCallback(), new Delete.DisabledCallback());
            new SFTPDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(),
                    new Delete.DisabledCallback());
        }
        session.close();
    }
}
