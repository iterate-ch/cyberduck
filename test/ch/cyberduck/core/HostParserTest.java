package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class HostParserTest extends AbstractTestCase {

    @Test
    public void testParseURLEmpty() {
        Host h = HostParser.parse("");
        assertTrue(h.getHostname().equals(PreferencesFactory.get().getProperty("connection.hostname.default")));
    }

    @Test
    public void testParseHostnameOnly() {
        assertEquals(HostParser.parse("hostname").getHostname(), "hostname");
        assertEquals(HostParser.parse("hostname ").getHostname(), "hostname");
        assertEquals(HostParser.parse(" hostname").getHostname(), "hostname");
    }

    @Test
    public void testParseHostnameOnlyRemoveTrailingSlash() {
        assertEquals(HostParser.parse("hostname/").getHostname(), "hostname");
        assertEquals(HostParser.parse("hostname//").getHostname(), "hostname");
        assertEquals(HostParser.parse("/hostname").getHostname(), "");
    }

    @Test
    public void testHostnameAppendPort() {
        assertTrue(HostParser.parse("s3.amazonaws.com:443").getHostname().equals("s3.amazonaws.com"));
        assertTrue(HostParser.parse("s3.amazonaws.com:443/").getHostname().equals("s3.amazonaws.com"));
        assertTrue(HostParser.parse("s3.amazonaws.com:443").getPort() == (443));
        assertTrue(HostParser.parse("s3.amazonaws.com:443/").getPort() == (443));
    }

    @Test
    public void testParseSftp() {
        String url = "sftp://user:pass@hostname/path/to/file";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(new SFTPProtocol()));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNotNull(h.getCredentials().getPassword());
        assertTrue(h.getCredentials().getPassword().equals("pass"));
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseFtp() {
        String url = "ftp://user:pass@hostname/path/to/file";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(new FTPProtocol()));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNotNull(h.getCredentials().getPassword());
        assertTrue(h.getCredentials().getPassword().equals("pass"));
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseFtps() {
        String url = "ftps://user:pass@hostname/path/to/file";
        Host h = HostParser.parse(url);
        assertTrue(h.getHostname().equals("hostname"));
        assertTrue(h.getProtocol().equals(new FTPTLSProtocol()));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNotNull(h.getCredentials().getPassword());
        assertTrue(h.getCredentials().getPassword().equals("pass"));
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseHttp() {
        String url = "http://www.testrumpus.com/";
        Host h = HostParser.parse(url);
        assertTrue(h.getHostname().equals("www.testrumpus.com"));
        assertTrue(h.getProtocol().equals(new DAVProtocol()));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getDefaultPath().equals("/"));
    }

    @Test
    public void testParseSftpWithPortNumber() {
        String url = "sftp://user:pass@hostname:999/path/to/file";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(new SFTPProtocol()));
        assertEquals(999, h.getPort());
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNotNull(h.getCredentials().getPassword());
        assertTrue(h.getCredentials().getPassword().equals("pass"));
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseFtpWithPortNumber() {
        String url = "ftp://user:pass@hostname:999/path/to/file";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(new FTPProtocol()));
        assertEquals(999, h.getPort());
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNotNull(h.getCredentials().getPassword());
        assertTrue(h.getCredentials().getPassword().equals("pass"));
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseFtpsWithPortNumber() {
        String url = "ftps://user:pass@hostname:999/path/to/file";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(new FTPTLSProtocol()));
        assertEquals(999, h.getPort());
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNotNull(h.getCredentials().getPassword());
        assertTrue(h.getCredentials().getPassword().equals("pass"));
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseSftpWithUsername() {
        String url = "sftp://user@hostname/path/to/file";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(new SFTPProtocol()));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNull(h.getCredentials().getPassword());
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseFtpWithUsername() {
        String url = "ftp://user@hostname/path/to/file";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(new FTPProtocol()));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNull(h.getCredentials().getPassword());
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseFtpsWithUsername() {
        String url = "ftps://user@hostname/path/to/file";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(new FTPTLSProtocol()));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNull(h.getCredentials().getPassword());
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseNoProtocolAndCustomPath() {
        String url = "user@hostname/path/to/file";
        Host h = HostParser.parse(url);
        assertTrue(h.getHostname().equals("hostname"));
        assertTrue(h.getProtocol().equals(
                ProtocolFactory.forName(PreferencesFactory.get().getProperty("connection.protocol.default"))));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNull(h.getCredentials().getPassword());
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseNoProtocol() {
        String url = "user@hostname";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(
                ProtocolFactory.forName(PreferencesFactory.get().getProperty("connection.protocol.default"))));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user"));
        assertNull(h.getCredentials().getPassword());
    }

    @Test
    public void testParseWithTwoAtSymbol() {
        String url = "user@name@hostname";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(
                ProtocolFactory.forName(PreferencesFactory.get().getProperty("connection.protocol.default"))));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user@name"));
        assertNull(h.getCredentials().getPassword());
    }

    @Test
    public void testParseWithTwoAtSymbolAndPassword() {
        String url = "user@name:password@hostname";
        Host h = HostParser.parse(url);
        assertEquals("hostname", h.getHostname());
        assertTrue(h.getProtocol().equals(
                ProtocolFactory.forName(PreferencesFactory.get().getProperty("connection.protocol.default"))));
        assertNotNull(h.getCredentials().getUsername());
        assertTrue(h.getCredentials().getUsername().equals("user@name"));
        assertTrue(h.getCredentials().getPassword().equals("password"));
    }

    @Test
    public void testParseWithDefaultPath() {
        String url = "user@hostname/path/to/file";
        Host h = HostParser.parse(url);
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseWithDefaultPathAndCustomPort() {
        String url = "user@hostname:999/path/to/file";
        Host h = HostParser.parse(url);
        assertTrue(h.getDefaultPath().equals("/path/to/file"));
    }

    @Test
    public void testParseDefaultPathWithAtSign() {
        final Host host = HostParser.parse("https://mail.sgbio.com/dav/YourEmail@sgbio.com/Briefcase");
        assertEquals("mail.sgbio.com", host.getHostname());
        assertEquals(ProtocolFactory.WEBDAV_SSL, host.getProtocol());
        assertEquals(Scheme.https, host.getProtocol().getScheme());
        assertEquals("/dav/YourEmail@sgbio.com/Briefcase", host.getDefaultPath());
        assertEquals("anonymous", host.getCredentials().getUsername());
    }

    @Test
    public void testParseUsernameDefaultPathWithAtSign() {
        final Host host = HostParser.parse("https://u@mail.sgbio.com/dav/YourEmail@sgbio.com/Briefcase");
        assertEquals("mail.sgbio.com", host.getHostname());
        assertEquals(ProtocolFactory.WEBDAV_SSL, host.getProtocol());
        assertEquals(Scheme.https, host.getProtocol().getScheme());
        assertEquals("/dav/YourEmail@sgbio.com/Briefcase", host.getDefaultPath());
        assertEquals("u", host.getCredentials().getUsername());
    }

    @Test
    public void testParseUsernamePasswordDefaultPathWithAtSign() {
        final Host host = HostParser.parse("https://u:p@mail.sgbio.com/dav/YourEmail@sgbio.com/Briefcase");
        assertEquals("mail.sgbio.com", host.getHostname());
        assertEquals(ProtocolFactory.WEBDAV_SSL, host.getProtocol());
        assertEquals(Scheme.https, host.getProtocol().getScheme());
        assertEquals("/dav/YourEmail@sgbio.com/Briefcase", host.getDefaultPath());
        assertEquals("u", host.getCredentials().getUsername());
        assertEquals("p", host.getCredentials().getPassword());
    }

    @Test
    public void testParseColonInPath() {
        final Host host = HostParser.parse("rackspace://cdn.duck.sh/duck-4.6.2.16174:16179M.pkg");
        assertEquals("identity.api.rackspacecloud.com", host.getHostname());
        assertEquals(Protocol.Type.swift, host.getProtocol().getType());
        assertEquals(443, host.getPort());
        assertEquals("/cdn.duck.sh/duck-4.6.2.16174:16179M.pkg", host.getDefaultPath());
    }

    @Test
    public void testParseColonInPathPlusPort() {
        final Host host = HostParser.parse("rackspace://cdn.duck.sh:444/duck-4.6.2.16174:16179M.pkg");
        assertEquals("identity.api.rackspacecloud.com", host.getHostname());
        assertEquals(Protocol.Type.swift, host.getProtocol().getType());
        assertEquals(444, host.getPort());
        assertEquals("/cdn.duck.sh/duck-4.6.2.16174:16179M.pkg", host.getDefaultPath());
    }

    @Test
    public void testInvalidPortnumber() {
        final Host host = HostParser.parse("ftp://hostname:21a");
        assertEquals("hostname", host.getHostname());
        assertEquals(Protocol.Type.ftp, host.getProtocol().getType());
        assertEquals(21, host.getPort());
    }

    @Test
    public void testMissingPortNumber() {
        final Host host = HostParser.parse("ftp://hostname:~/sandbox");
        assertEquals("hostname", host.getHostname());
        assertEquals(Protocol.Type.ftp, host.getProtocol().getType());
        assertEquals(21, host.getPort());
        assertEquals("~/sandbox", host.getDefaultPath());
    }

    @Test
    public void testParseIpv6() throws Exception {
        assertEquals("fc01:2:3:4:5::1", HostParser.parse("ftp://fc01:2:3:4:5::1/~/sandbox").getHostname());
        assertEquals(Protocol.Type.ftp, HostParser.parse("ftp://fc01:2:3:4:5::1/~/sandbox").getProtocol().getType());
        assertEquals(21, HostParser.parse("ftp://fc01:2:3:4:5::1/~/sandbox").getPort());
        assertEquals("user", HostParser.parse("ftp://user@fc01:2:3:4:5::1/~/sandbox").getCredentials().getUsername());
        assertEquals("/~/sandbox", HostParser.parse("ftp://fc01:2:3:4:5::1/~/sandbox").getDefaultPath());
        assertEquals("fc01:2:3:4:5::1", HostParser.parse("ftp://[fc01:2:3:4:5::1]:2121").getHostname());
        assertEquals(2121, HostParser.parse("ftp://[fc01:2:3:4:5::1]:2121").getPort());
        assertEquals("user", HostParser.parse("ftp://user@[fc01:2:3:4:5::1]:2121").getCredentials().getUsername());
        assertEquals("/~/sandbox", HostParser.parse("ftp://[fc01:2:3:4:5::1]:2121/~/sandbox").getDefaultPath());
        assertEquals("/sandbox", HostParser.parse("ftp://[fc01:2:3:4:5::1]:2121/sandbox").getDefaultPath());
        assertEquals("/sandbox@a", HostParser.parse("ftp://[fc01:2:3:4:5::1]:2121/sandbox@a").getDefaultPath());
    }

    @Test
    public void testParseIpv6LinkLocalZoneIndex() throws Exception {
        assertEquals("fe80::c62c:3ff:fe0b:8670%en0", HostParser.parse("ftp://fe80::c62c:3ff:fe0b:8670%en0/~/sandbox").getHostname());
    }

    @Test
    public void testParseS3Scheme() throws Exception {
        final Host host = HostParser.parse("s3://bucketname/key");
        assertEquals("s3.amazonaws.com", host.getHostname());
        assertEquals(Protocol.Type.s3, host.getProtocol().getType());
        assertEquals("/bucketname/key", host.getDefaultPath());
    }

    @Test
    public void testParseS3SchemeAccessKey() throws Exception {
        assertTrue(new Host(ProtocolFactory.S3_SSL, "s3.amazonaws.com", 443, "/cyberduck-test/key", new Credentials("AWS456", null))
                .compareTo(HostParser.parse("s3://AWS456@cyberduck-test/key")) == 0);
    }

    @Test
    public void testParseRackspaceScheme() throws Exception {
        assertTrue(new Host(ProtocolFactory.forName("rackspace"), "identity.api.rackspacecloud.com", 443, "/cdn.cyberduck.ch/", new Credentials("u", null))
                .compareTo(HostParser.parse("rackspace://u@cdn.cyberduck.ch/")) == 0);

    }
}
