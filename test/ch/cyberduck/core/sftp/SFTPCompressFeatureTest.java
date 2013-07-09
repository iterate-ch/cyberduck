package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Archive;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.features.Touch;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class SFTPCompressFeatureTest extends AbstractTestCase {

    @Test
    public void testArchive() throws Exception {
        final Host host = new Host(Protocol.SFTP, "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final SFTPCompressFeature feature = new SFTPCompressFeature(session);
        for(Archive archive : Archive.getKnownArchives()) {
            final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.FILE_TYPE);
            session.getFeature(Touch.class, null).touch(test);
            feature.archive(archive, Collections.<Path>singletonList(test));
            assertTrue(session.exists(archive.getArchive(Collections.<Path>singletonList(test))));
            session.delete(test, new DisabledLoginController());
            assertFalse(session.exists(test));
            feature.unarchive(archive, archive.getArchive(Collections.<Path>singletonList(test)));
            assertTrue(session.exists(test));
        }
        session.close();
    }
}
