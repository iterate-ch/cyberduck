package ch.cyberduck.core;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SystemConfigurationProxyTest extends AbstractTestCase {

    @Test
    public void testFind() throws Exception {
        final SystemConfigurationProxy proxy = new SystemConfigurationProxy();
        assertEquals(Proxy.Type.DIRECT, proxy.find(new Host("cyberduck.io")).getType());
        assertEquals(Proxy.Type.DIRECT, proxy.find(new Host(ProtocolFactory.WEBDAV, "cyberduck.io")).getType());
        assertEquals(Proxy.Type.DIRECT, proxy.find(new Host(ProtocolFactory.WEBDAV_SSL, "cyberduck.io")).getType());
    }

    @Test
    public void testPassiveFTP() throws Exception {
        final ProxyFinder proxy = new SystemConfigurationProxy();
        assertTrue(proxy.usePassiveFTP());
    }

    @Test
    public void testIsExcludedCidrNotation() throws Exception {
        final SystemConfigurationProxy proxy = new SystemConfigurationProxy();
        assertFalse(proxy.isExcluded(new Host("cyberduck.io"), "192.168.1.0/32"));
        assertTrue(proxy.isExcluded(new Host("192.168.1.10"), "192.168.1.0/24"));
    }

    @Test
    public void testExcludedLocalHost() throws Exception {
        final SystemConfigurationProxy proxy = new SystemConfigurationProxy();
        assertTrue(proxy.isExcluded(new Host("cyberduck.local")));
        assertFalse(proxy.isExcluded(new Host("cyberduck.io")));
    }

    @Test
    public void testExceptions() throws Exception {
        final SystemConfigurationProxy proxy = new SystemConfigurationProxy();
        final String[] list = proxy.getProxyExceptionsNative();
        assertNotNull(list);
        assertTrue(Arrays.asList(list).contains("*.local"));
        assertTrue(Arrays.asList(list).contains("169.254/16"));
    }

    @Test
    public void testSimpleExcluded() throws Exception {
        final SystemConfigurationProxy proxy = new SystemConfigurationProxy();
        assertTrue(proxy.isSimpleHostnameExcludedNative());
        assertEquals(Proxy.Type.DIRECT, proxy.find(new Host(ProtocolFactory.WEBDAV, "simple")).getType());
    }
}
