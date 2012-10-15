package ch.cyberduck.core;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SystemConfigurationProxyTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        SystemConfigurationProxy.register();
    }

    @Test
    public void testConfigure() throws Exception {
        final Proxy proxy = ProxyFactory.get();
        assertFalse(proxy.isSOCKSProxyEnabled(new Host("t")));
        assertFalse(proxy.isHTTPProxyEnabled(new Host("t")));
        assertFalse(proxy.isHTTPSProxyEnabled(new Host("t")));
        assertTrue(proxy.usePassiveFTP());
    }
}
