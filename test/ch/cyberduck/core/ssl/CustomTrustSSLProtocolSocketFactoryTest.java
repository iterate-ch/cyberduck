package ch.cyberduck.core.ssl;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id:$
 */
public class CustomTrustSSLProtocolSocketFactoryTest {

    @Test
    public void testGetSSLContext() throws Exception {
        assertNotNull(new CustomTrustSSLProtocolSocketFactory(new DefaultX509TrustManager()).getSSLContext());
    }
}
