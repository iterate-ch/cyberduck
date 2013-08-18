package ch.cyberduck.core.shared;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class DefaultUrlProviderTest extends AbstractTestCase {

    @Test
    public void testDav() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/my/documentroot");
        final DAVSession session = new DAVSession(host);
        assertEquals("http://test.cyberduck.ch/my/documentroot/f",
                new DefaultUrlProvider(host).toUrl(new Path("/my/documentroot/f", Path.DIRECTORY_TYPE)).find(DescriptiveUrl.Type.provider).getUrl());
        assertEquals("http://test.cyberduck.ch/f",
                new DefaultUrlProvider(host).toUrl(new Path("/my/documentroot/f", Path.DIRECTORY_TYPE)).find(DescriptiveUrl.Type.http).getUrl());
    }


    @Test
    public void testSftp() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/my/documentroot");
        final SFTPSession session = new SFTPSession(host);
        assertEquals("sftp://test.cyberduck.ch/my/documentroot/f",
                new DefaultUrlProvider(host).toUrl(new Path("/my/documentroot/f", Path.DIRECTORY_TYPE)).find(DescriptiveUrl.Type.provider).getUrl());
        assertEquals("http://test.cyberduck.ch/f",
                new DefaultUrlProvider(host).toUrl(new Path("/my/documentroot/f", Path.DIRECTORY_TYPE)).find(DescriptiveUrl.Type.http).getUrl());
    }

    @Test
    public void testAbsoluteDocumentRoot() {
        Host host = new Host("localhost");
        host.setDefaultPath("/usr/home/dkocher/public_html");
        final Session session = SessionFactory.createSession(host);
        Path path = new Path(
                "/usr/home/dkocher/public_html/file", Path.DIRECTORY_TYPE);
        assertEquals("http://localhost/file", new DefaultUrlProvider(host).toUrl(path).find(DescriptiveUrl.Type.http).getUrl());
        host.setWebURL("http://127.0.0.1/~dkocher");
        assertEquals("http://127.0.0.1/~dkocher/file", new DefaultUrlProvider(host).toUrl(path).find(DescriptiveUrl.Type.http).getUrl());
    }

    @Test
    public void testRelativeDocumentRoot() {
        Host host = new Host("localhost");
        host.setDefaultPath("public_html");
        final Session session = SessionFactory.createSession(host);
        Path path = new Path(
                "/usr/home/dkocher/public_html/file", Path.DIRECTORY_TYPE);
        assertEquals("http://localhost/file", new DefaultUrlProvider(host).toUrl(path).find(DescriptiveUrl.Type.http).getUrl());
        host.setWebURL("http://127.0.0.1/~dkocher");
        assertEquals("http://127.0.0.1/~dkocher/file", new DefaultUrlProvider(host).toUrl(path).find(DescriptiveUrl.Type.http).getUrl());
    }

    @Test
    public void testDefaultPathRoot() {
        Host host = new Host("localhost");
        host.setDefaultPath("/");
        final Session session = SessionFactory.createSession(host);
        Path path = new Path(
                "/file", Path.DIRECTORY_TYPE);
        assertEquals("http://localhost/file", new DefaultUrlProvider(host).toUrl(path).find(DescriptiveUrl.Type.http).getUrl());
        host.setWebURL("http://127.0.0.1/~dkocher");
        assertEquals("http://127.0.0.1/~dkocher/file", new DefaultUrlProvider(host).toUrl(path).find(DescriptiveUrl.Type.http).getUrl());
    }
}
