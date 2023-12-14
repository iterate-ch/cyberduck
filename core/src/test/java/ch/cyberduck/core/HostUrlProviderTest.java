package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HostUrlProviderTest {

    @Test
    public void testToUrl() {
        assertEquals("sftp://user@localhost", new HostUrlProvider().get(new Host(new TestProtocol(Scheme.sftp), "localhost", new Credentials("user", "p"))));
        assertEquals("sftp://localhost", new HostUrlProvider().withUsername(false).withPath(false).get(new Host(new TestProtocol(Scheme.sftp), "localhost", new Credentials("user", "p"))));
        assertEquals("sftp://localhost:222",
            new HostUrlProvider().withUsername(false).withPath(false).get(new Host(new TestProtocol(Scheme.sftp), "localhost", 222)));
    }

    @Test
    public void testPath() {
        final Host h = new Host(new TestProtocol(Scheme.sftp), "localhost", new Credentials("user", "p"));
        h.setDefaultPath("p/p p");
        assertEquals("sftp://user@localhost/p/p%20p", new HostUrlProvider().withUsername(true).withPath(true).get(h));
    }

    @Test
    public void testOAuth() {
        final Host h = new Host(new TestProtocol(Scheme.https), "login.microsoftonline.com", new Credentials("Microsoft OneDrive (t@domain.com) OAuth2 Access Token"));
        assertEquals("https://Microsoft%20OneDrive%20%28t@domain.com%29%20OAuth2%20Access%20Token@login.microsoftonline.com", new HostUrlProvider().withUsername(true).withPath(true).get(h));
    }
}
