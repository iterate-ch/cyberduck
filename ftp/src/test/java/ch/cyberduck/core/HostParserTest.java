package ch.cyberduck.core;

import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.ftp.FTPProtocol;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class HostParserTest {

    @Test(expected = HostParserException.class)
    public void testParseURLEmpty() throws HostParserException {
        Host h = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get("");
        assertEquals("", h.getHostname());
    }

    @Test
    public void testParseHostnameOnly() throws HostParserException {
        assertEquals("hostname", new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get("hostname").getHostname());
        assertEquals("hostname", new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get("hostname ").getHostname());
        assertEquals("hostname", new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get(" hostname").getHostname());
    }

    @Test
    public void testParseHostnameOnlyRemoveTrailingSlash() throws HostParserException {
        assertEquals("hostname", new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get("hostname/").getHostname());
        assertEquals("hostname", new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get("hostname//").getHostname());
        assertEquals("", new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get("/hostname").getHostname());
    }

    @Test
    public void testParseNoProtocolAndCustomPath() throws HostParserException {
        String url = "user@hostname/path/to/file";
        Host h = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get(url);
        assertEquals("hostname", h.getHostname());
        assertNotNull(h.getCredentials().getUsername());
        assertEquals("user", h.getCredentials().getUsername());
        assertNull(h.getCredentials().getPassword());
        assertEquals("/path/to/file", h.getDefaultPath());
    }

    @Test
    public void testParseNoProtocol() throws HostParserException {
        String url = "user@hostname";
        Host h = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get(url);
        assertEquals("hostname", h.getHostname());
        assertNotNull(h.getCredentials().getUsername());
        assertEquals("user", h.getCredentials().getUsername());
        assertNull(h.getCredentials().getPassword());
    }

    @Test
    public void testParseWithTwoAtSymbol() throws HostParserException {
        String url = "user@name@hostname";
        Host h = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get(url);
        assertEquals("hostname", h.getHostname());
        assertNotNull(h.getCredentials().getUsername());
        assertEquals("user@name", h.getCredentials().getUsername());
        assertNull(h.getCredentials().getPassword());
    }

    @Test
    public void testParseWithTwoAtSymbolAndPassword() throws HostParserException {
        String url = "user@name:password@hostname";
        Host h = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get(url);
        assertEquals("hostname", h.getHostname());
        assertNotNull(h.getCredentials().getUsername());
        assertEquals("user@name", h.getCredentials().getUsername());
        assertEquals("password", h.getCredentials().getPassword());
    }

    @Test
    public void testParseWithDefaultPath() throws HostParserException {
        String url = "user@hostname/path/to/file";
        Host h = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get(url);
        assertEquals("/path/to/file", h.getDefaultPath());
    }

    @Test
    public void testParseWithDefaultPathAndCustomPort() throws HostParserException {
        String url = "user@hostname:999/path/to/file";
        Host h = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get(url);
        assertEquals("/path/to/file", h.getDefaultPath());
    }

    @Test
    public void testInvalidPortnumber() throws HostParserException {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get("ftp://hostname:21a");
        assertEquals("hostname", host.getHostname());
        assertEquals(Protocol.Type.ftp, host.getProtocol().getType());
        assertEquals(21, host.getPort());
    }

    @Test
    public void testMissingPortNumber() throws HostParserException {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get("ftp://hostname:~/sandbox");
        assertEquals("hostname", host.getHostname());
        assertEquals(Protocol.Type.ftp, host.getProtocol().getType());
        assertEquals(21, host.getPort());
        assertEquals("~/sandbox", host.getDefaultPath());
    }

    @Test
    public void testParseIpv6() throws HostParserException {
        final HostParser parser = new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol())));
        assertEquals("fc01:2:3:4:5::1", parser.get("ftp://[fc01:2:3:4:5::1]:2121").getHostname());
        assertEquals(2121, parser.get("ftp://[fc01:2:3:4:5::1]:2121").getPort());
        assertEquals("user", parser.get("ftp://user@[fc01:2:3:4:5::1]:2121").getCredentials().getUsername());
        assertEquals("/~/sandbox", parser.get("ftp://[fc01:2:3:4:5::1]:2121/~/sandbox").getDefaultPath());
        assertEquals("/sandbox", parser.get("ftp://[fc01:2:3:4:5::1]:2121/sandbox").getDefaultPath());
        assertEquals("/sandbox@a", parser.get("ftp://[fc01:2:3:4:5::1]:2121/sandbox@a").getDefaultPath());
    }

    @Test
    public void testParseIpv6LinkLocalZoneIndex() throws HostParserException {
        assertEquals("fe80::c62c:3ff:fe0b:8670%en0", new HostParser(new ProtocolFactory(Collections.singleton(new TestFTPProtocol()))).get("ftp://[fe80::c62c:3ff:fe0b:8670%en0]/~/sandbox").getHostname());
    }

    private static class TestFTPProtocol extends FTPProtocol {
        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
