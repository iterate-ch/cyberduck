package ch.cyberduck.core;

import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class SessionFactoryTest extends AbstractTestCase {

    @Test
    public void testCreateSession() throws Exception {
        for(Protocol protocol : ProtocolFactory.getEnabledProtocols()) {
            final Host host = new Host(protocol, "h");
            assertNotNull(SessionFactory.create(host,
                    new KeychainX509TrustManager(new DefaultTrustManagerHostnameCallback(host)), new KeychainX509KeyManager()));
        }
    }
}
