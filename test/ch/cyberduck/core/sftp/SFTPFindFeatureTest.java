package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SFTPFindFeatureTest extends AbstractTestCase {

    @Test
    public void testFindNotFound() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertFalse(new SFTPFindFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
        session.close();
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertTrue(new SFTPFindFeature(session).find(session.workdir()));
        assertTrue(new SFTPFindFeature(session).find(new Path(session.workdir(), ".ssh", EnumSet.of(AbstractPath.Type.directory))));
        session.close();
    }

    @Test
    public void testFindFile() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertTrue(new SFTPFindFeature(session).find(new Path(session.workdir(), ".bash_profile", EnumSet.of(AbstractPath.Type.file))));
        session.close();
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new SFTPFindFeature(new SFTPSession(new Host("h"))).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}
