package ch.cyberduck.ui.action;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class MountWorkerTest extends AbstractTestCase {

    @Test
    public void testRun() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        host.setDefaultPath("/notfound");
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Cache cache = new Cache();
        final MountWorker worker = new MountWorker(session, cache, new DisabledListProgressListener());
        assertEquals(new Path("/home/jenkins", EnumSet.of(Path.Type.directory)), worker.run());
        assertTrue(cache.containsKey(new Path("/home/jenkins", EnumSet.of(Path.Type.directory)).getReference()));
        assertTrue(cache.containsKey(new Path("/notfound", EnumSet.of(Path.Type.directory)).getReference()));
        session.close();
    }
}
