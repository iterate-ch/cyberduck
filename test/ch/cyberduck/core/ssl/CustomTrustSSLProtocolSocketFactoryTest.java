package ch.cyberduck.core.ssl;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @version $Id$
 */
public class CustomTrustSSLProtocolSocketFactoryTest extends AbstractTestCase {

    @Test
    public void testGetSSLContext() throws Exception {
        assertNotNull(new CustomTrustSSLProtocolSocketFactory(new DefaultX509TrustManager()).getSSLContext());
    }
}
