package ch.cyberduck.core.shared;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class DefaultHomeFinderServiceTest extends AbstractTestCase {

    @Test
    public void testFindFtp() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DefaultHomeFinderService(session).find());
        session.close();
    }

    @Test
    public void testFindDefaultDirectory() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("ftp.user"), properties.getProperty("ftp.password")
        ));
        host.setDefaultPath("/test.d");
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertEquals(new Path("/test.d", EnumSet.of(Path.Type.directory)), new DefaultHomeFinderService(session).find());
        session.close();
    }

    @Test
    public void testFindSftp() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                properties.getProperty("sftp.user"), properties.getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback(), new DisabledTranscriptListener());
        assertEquals(new Path("/home/jenkins", EnumSet.of(Path.Type.directory)), new DefaultHomeFinderService(session).find());
        session.close();
    }

    @Test
    public void testFind() throws Exception {
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
                new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "sandbox"));
        assertEquals(new Path("/sandbox", EnumSet.of(Path.Type.directory)),
                new DefaultHomeFinderService(null).find(new Path("/", EnumSet.of(Path.Type.directory)), "/sandbox"));
    }
}
