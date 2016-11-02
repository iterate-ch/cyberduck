package ch.cyberduck.core.sftp;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.local.DefaultLocalTouchFeature;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SFTPPublicKeyAuthenticationTest {

    @Test
    public void testAuthenticateKeyNoPassword() throws Exception {
        final Credentials credentials = new Credentials(
                System.getProperties().getProperty("sftp.user")
        );
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            credentials.setIdentity(key);
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(new StringReader(System.getProperties().getProperty("sftp.key")), key.getOutputStream(false), Charset.forName("UTF-8"));
            final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
            final SFTPSession session = new SFTPSession(host);
            session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
            assertTrue(new SFTPPublicKeyAuthentication(session, new DisabledPasswordStore()).authenticate(host, new DisabledLoginCallback() {
                @Override
                public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                    fail();
                }
            }, new DisabledCancelCallback()));
            session.close();
        }
        finally {
            key.delete();
        }
    }

    @Test(expected = LoginFailureException.class)
    public void testAuthenticatePuTTYKeyWithWrongPassword() throws Exception {
        final Credentials credentials = new Credentials(
                System.getProperties().getProperty("sftp.user"), ""
        );
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            credentials.setIdentity(key);
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(new StringReader(System.getProperties().getProperty("sftp.key.putty")), key.getOutputStream(false), Charset.forName("UTF-8"));
            final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
            final SFTPSession session = new SFTPSession(host);
            session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
            final AtomicBoolean p = new AtomicBoolean();
            assertFalse(new SFTPPublicKeyAuthentication(session, new DisabledPasswordStore()).authenticate(host, new DisabledLoginCallback() {
                @Override
                public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                    p.set(true);
                }
            }, new DisabledCancelCallback()));
            assertTrue(p.get());
            session.close();
        }
        finally {
            key.delete();
        }
    }

    @Test(expected = LoginFailureException.class)
    public void testAuthenticateOpenSSHKeyWithPassword() throws Exception {
        final Credentials credentials = new Credentials(
                System.getProperties().getProperty("sftp.user"), ""
        );
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            credentials.setIdentity(key);
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(new StringReader(System.getProperties().getProperty("sftp.key.openssh.rsa")), key.getOutputStream(false), Charset.forName("UTF-8"));
            final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
            final SFTPSession session = new SFTPSession(host);
            session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
            final AtomicBoolean p = new AtomicBoolean();
            assertTrue(new SFTPPublicKeyAuthentication(session, new DisabledPasswordStore()).authenticate(host, new DisabledLoginCallback() {
                @Override
                public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                    p.set(true);
                }
            }, new DisabledCancelCallback()));
            assertTrue(p.get());
            session.close();
        }
        finally {
            key.delete();
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testUnknownFormat() throws Exception {
        final Credentials credentials = new Credentials(
                System.getProperties().getProperty("sftp.user"), ""
        );
        final Local key = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            credentials.setIdentity(key);
            new DefaultLocalTouchFeature().touch(key);
            IOUtils.copy(new StringReader("--unknown format"), key.getOutputStream(false), Charset.forName("UTF-8"));
            final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
            final SFTPSession session = new SFTPSession(host);
            session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
            assertTrue(new SFTPPublicKeyAuthentication(session, new DisabledPasswordStore()).authenticate(host, new DisabledLoginCallback() {
                @Override
                public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options) throws LoginCanceledException {
                    fail();
                }
            }, new DisabledCancelCallback()));
            session.close();
        }
        finally {
            key.delete();
        }
    }
}
