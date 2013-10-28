package ch.cyberduck.core;

import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class HostUrlProviderTest extends AbstractTestCase {

    @Test
    public void testToUrl() {
        assertEquals("sftp://user@localhost", new HostUrlProvider().get(new Host(new SFTPProtocol(), "localhost", new Credentials("user", "p"))));
        assertEquals("sftp://localhost", new HostUrlProvider(false, false).get(new Host(new SFTPProtocol(), "localhost", new Credentials("user", "p"))));
        assertEquals("sftp://localhost:222",
                new HostUrlProvider(false, false).get(new Host(new SFTPProtocol(), "localhost", 222)));
    }

    @Test
    public void testPath() {
        final Host h = new Host(new SFTPProtocol(), "localhost", new Credentials("user", "p"));
        h.setDefaultPath("p");
        assertEquals("sftp://user@localhost/p", new HostUrlProvider(true, true).get(h));
    }
}
