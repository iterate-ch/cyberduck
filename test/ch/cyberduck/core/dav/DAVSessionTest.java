package ch.cyberduck.core.dav;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.Controller;

import org.junit.Test;

import com.googlecode.sardine.impl.SardineException;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class DAVSessionTest extends AbstractTestCase {

    @Test(expected = SardineException.class)
    public void testSsl() throws Exception {
        final Host host = new Host(Protocol.WEBDAV_SSL, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.connect();
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginCancelBasicAuth() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.connect();
    }

    @Test
    public void testListAnonymous() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setDefaultPath("/dav/anon");
        final DAVSession session = new DAVSession(host);
        session.connect();
        assertNotNull(session.home().list());
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginFailureBasicAuth() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.connect();
    }

    @Test(expected = LoginCanceledException.class)
    public void testLoginFailureDigestAuth() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                "u", "p"
        ));
        host.setDefaultPath("/dav/digest");
        final DAVSession session = new DAVSession(host);
        LoginControllerFactory.addFactory(Factory.NATIVE_PLATFORM, new LoginControllerFactory() {
            @Override
            protected LoginController create(final Controller c) {
                return create();
            }

            @Override
            protected LoginController create(final Session s) {
                return create();
            }

            @Override
            protected LoginController create() {
                return new AbstractLoginController() {
                    @Override
                    public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason,
                                       final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
                        assertEquals(host.getCredentials(), credentials);
                        assertEquals("Login failed", title);
                        assertEquals("Authorization Required", reason);
                        assertFalse(enablePublicKey);
                        throw new LoginCanceledException();
                    }
                };
            }
        });
        session.connect();
    }
}
