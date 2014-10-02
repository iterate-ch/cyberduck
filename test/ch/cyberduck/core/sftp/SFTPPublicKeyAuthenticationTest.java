package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LocalTouchFactory;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.StringReader;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SFTPPublicKeyAuthenticationTest extends AbstractTestCase {

    @Test
    public void testAuthenticateKeyNoPassword() throws Exception {
        final Credentials credentials = new Credentials(
                properties.getProperty("sftp.user"), null, false
        );
        final FinderLocal key = new FinderLocal(System.getProperty("java.io.tmpdir"), "k");
        credentials.setIdentity(key);
        LocalTouchFactory.get().touch(key);
        IOUtils.copy(new StringReader(properties.getProperty("sftp.key")), key.getOutputStream(false));
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), session);
        assertTrue(new SFTPPublicKeyAuthentication(session).authenticate(host, new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                fail();
            }
        }, new DisabledCancelCallback()));
        session.close();
        key.delete();
    }

    @Test(expected = LoginFailureException.class)
    public void testAuthenticatePuTTYKeyWithWrongPassword() throws Exception {
        final Credentials credentials = new Credentials(
                properties.getProperty("sftp.user"), "", false
        );
        final FinderLocal key = new FinderLocal(System.getProperty("java.io.tmpdir"), "k");
        credentials.setIdentity(key);
        LocalTouchFactory.get().touch(key);
        IOUtils.copy(new StringReader(properties.getProperty("sftp.key.putty")), key.getOutputStream(false));
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), session);
        final AtomicBoolean p = new AtomicBoolean();
        assertFalse(new SFTPPublicKeyAuthentication(session).authenticate(host, new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                p.set(true);
            }
        }, new DisabledCancelCallback()));
        assertTrue(p.get());
        session.close();
        key.delete();
    }

    @Test(expected = LoginFailureException.class)
    public void testAuthenticateOpenSSHKeyWithPassword() throws Exception {
        final Credentials credentials = new Credentials(
                properties.getProperty("sftp.user"), "", false
        );
        final FinderLocal key = new FinderLocal(System.getProperty("java.io.tmpdir"), "k");
        credentials.setIdentity(key);
        LocalTouchFactory.get().touch(key);
        IOUtils.copy(new StringReader(properties.getProperty("sftp.key.openssh.rsa")), key.getOutputStream(false));
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), session);
        final AtomicBoolean p = new AtomicBoolean();
        assertTrue(new SFTPPublicKeyAuthentication(session).authenticate(host, new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                p.set(true);
            }
        }, new DisabledCancelCallback()));
        assertTrue(p.get());
        session.close();
        key.delete();
    }

    @Test
    public void testAuthenticateECDSA() throws Exception {
        final Credentials credentials = new Credentials(
                properties.getProperty("sftp.user"), "", false
        );
        final FinderLocal key = new FinderLocal(System.getProperty("java.io.tmpdir"), "k");
        credentials.setIdentity(key);
        LocalTouchFactory.get().touch(key);
        IOUtils.copy(new StringReader(properties.getProperty("sftp.key.openssh.ecdsa")), key.getOutputStream(false));
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), session);
        assertTrue(new SFTPPublicKeyAuthentication(session).authenticate(host, new DisabledLoginController() {
            @Override
            public void prompt(Protocol protocol, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                fail();
            }
        }, new DisabledCancelCallback()));
        session.close();
        key.delete();
    }
}
