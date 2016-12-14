package ch.cyberduck.core.shared;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class DefaultHomeFinderServiceTest {

    @Test
    public void testFindFtp() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DefaultHomeFinderService(session).find());
        session.close();
    }

    @Test
    public void testFindDefaultDirectory() throws Exception {
        final Host host = new Host(new FTPTLSProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("ftp.user"), System.getProperties().getProperty("ftp.password")
        ));
        host.setDefaultPath("/test.d");
        final FTPSession session = new FTPSession(host);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        assertEquals(new Path("/test.d", EnumSet.of(Path.Type.directory)), new DefaultHomeFinderService(session).find());
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
