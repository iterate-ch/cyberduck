package ch.cyberduck.core.sftp;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.local.FinderLocal;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class SFTPPublicKeyAuthenticationTest extends AbstractTestCase {

    @Test
    public void testAuthenticate() throws Exception {
        final Credentials credentials = new Credentials(
                properties.getProperty("sftp.user"), null, false
        );
        final FinderLocal key = new FinderLocal(System.getProperty("java.io.tmpdir"), "k");
        credentials.setIdentity(key);
        key.touch();
        IOUtils.copy(new StringReader(properties.getProperty("sftp.key")), key.getOutputStream(false));
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", credentials);
        final SFTPSession session = new SFTPSession(host);
        session.open(new DefaultHostKeyController());
        assertTrue(new SFTPPublicKeyAuthentication(session).authenticate(host, new DisabledLoginController()));
        session.close();
        key.delete();
    }
}
