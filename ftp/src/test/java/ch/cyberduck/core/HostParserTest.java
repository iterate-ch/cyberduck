package ch.cyberduck.core;

import ch.cyberduck.core.ftp.FTPProtocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class HostParserTest extends AbstractTestCase {

    @Test
    public void testParseURLEmpty() {
        Host h = new HostParser(new FTPProtocol()).get("");
        assertEquals("", h.getHostname());
    }

    @Test
    public void testParseHostnameOnly() {
        assertEquals("hostname", new HostParser(new FTPProtocol()).get("hostname").getHostname());
        assertEquals("hostname", new HostParser(new FTPProtocol()).get("hostname ").getHostname());
        assertEquals("hostname", new HostParser(new FTPProtocol()).get(" hostname").getHostname());
    }

    @Test
    public void testParseHostnameOnlyRemoveTrailingSlash() {
        assertEquals("hostname", new HostParser(new FTPProtocol()).get("hostname/").getHostname());
        assertEquals("hostname", new HostParser(new FTPProtocol()).get("hostname//").getHostname());
        assertEquals("", new HostParser(new FTPProtocol()).get("/hostname").getHostname());
    }

    @Test
    public void testParseNoProtocolAndCustomPath() {
        String url = "user@hostname/path/to/file";
        Host h = new HostParser(new FTPProtocol()).get(url);
        assertEquals("hostname", h.getHostname());
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNull(h.getCredentials().getPassword());
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseNoProtocol() {
        String url = "user@hostname";
        Host h = new HostParser(new FTPProtocol()).get(url);
        assertEquals("hostname", h.getHostname());
        assertNotNull(h.getCredentials().getUsername());
        assertEquals("user", h.getCredentials().getUsername());
        assertNull(h.getCredentials().getPassword());
    }

    @Test
    public void testParseWithTwoAtSymbol() {
        String url = "user@name@hostname";
        Host h = new HostParser(new FTPProtocol()).get(url);
        assertEquals("hostname", h.getHostname());
        assertNotNull(h.getCredentials().getUsername());
        assertEquals("user@name", h.getCredentials().getUsername());
        assertNull(h.getCredentials().getPassword());
    }

    @Test
    public void testParseWithTwoAtSymbolAndPassword() {
        String url = "user@name:password@hostname";
        Host h = new HostParser(new FTPProtocol()).get(url);
        assertEquals("hostname", h.getHostname());
        assertNotNull(h.getCredentials().getUsername());
        assertEquals("user@name", h.getCredentials().getUsername());
        assertEquals("password", h.getCredentials().getPassword());
    }

    @Test
    public void testParseWithDefaultPath() {
        String url = "user@hostname/path/to/file";
        Host h = new HostParser(new FTPProtocol()).get(url);
        assertEquals("/path/to/file", h.getDefaultPath());
    }

    @Test
    public void testParseWithDefaultPathAndCustomPort() {
        String url = "user@hostname:999/path/to/file";
        Host h = new HostParser(new FTPProtocol()).get(url);
        assertEquals("/path/to/file", h.getDefaultPath());
    }

    @Test
    public void testInvalidPortnumber() {
        final Host host = new HostParser(new FTPProtocol()).get("ftp://hostname:21a");
        assertEquals("hostname", host.getHostname());
        assertEquals(Protocol.Type.ftp, host.getProtocol().getType());
        assertEquals(21, host.getPort());
    }

    @Test
    public void testMissingPortNumber() {
        final Host host = new HostParser(new FTPProtocol()).get("ftp://hostname:~/sandbox");
        assertEquals("hostname", host.getHostname());
        assertEquals(Protocol.Type.ftp, host.getProtocol().getType());
        assertEquals(21, host.getPort());
        assertEquals("~/sandbox", host.getDefaultPath());
    }

    @Test
    public void testParseIpv6() throws Exception {
        assertEquals("fc01:2:3:4:5::1", new HostParser(new FTPProtocol()).get("ftp://fc01:2:3:4:5::1/~/sandbox").getHostname());
        assertEquals(Protocol.Type.ftp, new HostParser(new FTPProtocol()).get("ftp://fc01:2:3:4:5::1/~/sandbox").getProtocol().getType());
        assertEquals(21, new HostParser(new FTPProtocol()).get("ftp://fc01:2:3:4:5::1/~/sandbox").getPort());
        assertEquals("user", new HostParser(new FTPProtocol()).get("ftp://user@fc01:2:3:4:5::1/~/sandbox").getCredentials().getUsername());
        assertEquals("/~/sandbox", new HostParser(new FTPProtocol()).get("ftp://fc01:2:3:4:5::1/~/sandbox").getDefaultPath());
        assertEquals("fc01:2:3:4:5::1", new HostParser(new FTPProtocol()).get("ftp://[fc01:2:3:4:5::1]:2121").getHostname());
        assertEquals(2121, new HostParser(new FTPProtocol()).get("ftp://[fc01:2:3:4:5::1]:2121").getPort());
        assertEquals("user", new HostParser(new FTPProtocol()).get("ftp://user@[fc01:2:3:4:5::1]:2121").getCredentials().getUsername());
        assertEquals("/~/sandbox", new HostParser(new FTPProtocol()).get("ftp://[fc01:2:3:4:5::1]:2121/~/sandbox").getDefaultPath());
        assertEquals("/sandbox", new HostParser(new FTPProtocol()).get("ftp://[fc01:2:3:4:5::1]:2121/sandbox").getDefaultPath());
        assertEquals("/sandbox@a", new HostParser(new FTPProtocol()).get("ftp://[fc01:2:3:4:5::1]:2121/sandbox@a").getDefaultPath());
    }

    @Test
    public void testParseIpv6LinkLocalZoneIndex() throws Exception {
        assertEquals("fe80::c62c:3ff:fe0b:8670%en0", new HostParser(new FTPProtocol()).get("ftp://fe80::c62c:3ff:fe0b:8670%en0/~/sandbox").getHostname());
    }
}
