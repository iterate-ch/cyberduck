package ch.cyberduck.core.importer;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class FilezillaBookmarkCollectionTest {

    @Test
    public void testParse() throws Exception {
        FilezillaBookmarkCollection c = new FilezillaBookmarkCollection();
        assertEquals(0, c.size());
        c.parse(new ProtocolFactory(new HashSet<>(Arrays.asList(new TestProtocol(Scheme.ftp), new TestProtocol(Scheme.ftps), new TestProtocol(Scheme.sftp)))), new Local("src/test/resources/org.filezilla-project.sitemanager.xml"));
        assertEquals(2, c.size());
    }

    @Test
    public void testParseBase64EncodedPassword() throws Exception {
        final AtomicBoolean saved = new AtomicBoolean();
        FilezillaBookmarkCollection c = new FilezillaBookmarkCollection(new PasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPassword(final String hostname, final String user) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addPassword(final String serviceName, final String user, final String password) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
                assertEquals("test", password);
                saved.set(true);
            }
        });
        assertEquals(0, c.size());
        c.parse(new ProtocolFactory(new HashSet<>(Arrays.asList(new TestProtocol(Scheme.ftp), new TestProtocol(Scheme.ftps), new TestProtocol(Scheme.sftp)))), new Local("src/test/resources/org.filezilla-project.sitemanager-2.xml"));
        assertEquals(1, c.size());
        final Host b = c.iterator().next();
        assertTrue(saved.get());
        assertNull(b.getCredentials().getPassword());
    }
}