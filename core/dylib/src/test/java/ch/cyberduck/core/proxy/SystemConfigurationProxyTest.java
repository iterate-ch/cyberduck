package ch.cyberduck.core.proxy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SystemConfigurationProxyTest {

    @Test
    public void testFind() {
        final SystemConfigurationProxy proxy = new SystemConfigurationProxy();
        assertEquals(Proxy.Type.DIRECT, proxy.find("http://cyberduck.io").getType());
        assertEquals(Proxy.Type.DIRECT, proxy.find("sftp://cyberduck.io").getType());
        assertEquals(Proxy.Type.DIRECT, proxy.find("ftp://cyberduck.io").getType());
        assertEquals(Proxy.Type.DIRECT, proxy.find("ftps://cyberduck.io").getType());
    }

    @Test
    public void testExcludedLocalHost() {
        final SystemConfigurationProxy proxy = new SystemConfigurationProxy();
        assertEquals(Proxy.Type.DIRECT, proxy.find("http://cyberduck.local").getType());
    }

    @Test
    public void testSimpleExcluded() {
        final SystemConfigurationProxy proxy = new SystemConfigurationProxy();
        assertEquals(Proxy.Type.DIRECT, proxy.find("http://simple").getType());
    }
}
