package ch.cyberduck.core;

import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SessionFactoryTest {

    @Test
    public void testCreateSession() throws Exception {
        assertNotNull(SessionFactory.create(new Host(new S3Protocol()),
                new DefaultX509TrustManager(), new DefaultX509KeyManager(),
                new DisabledPasswordStore(), new DisabledLoginCallback()));
    }
}
