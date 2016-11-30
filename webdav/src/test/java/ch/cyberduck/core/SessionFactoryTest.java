package ch.cyberduck.core;

import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SessionFactoryTest {

    @Test
    public void testCreateSession() throws Exception {
        assertNotNull(SessionFactory.create(new Host(new DAVProtocol()),
                new DefaultX509TrustManager(), new DefaultX509KeyManager()));
        assertNotNull(SessionFactory.create(new Host(new DAVSSLProtocol()),
                new DefaultX509TrustManager(), new DefaultX509KeyManager()));
    }
}
