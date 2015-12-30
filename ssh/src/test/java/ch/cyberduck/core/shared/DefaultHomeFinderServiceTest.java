package ch.cyberduck.core.shared;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class DefaultHomeFinderServiceTest {

    @Test
    public void testFind() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertEquals(new Path("/home/jenkins", EnumSet.of(Path.Type.directory)), new DefaultHomeFinderService(session).find());
        session.close();
    }

    @Test
    public void testFindWithWorkdir() throws Exception {
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
                new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "sandbox"));
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
                new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "/sandbox"));
    }
}
