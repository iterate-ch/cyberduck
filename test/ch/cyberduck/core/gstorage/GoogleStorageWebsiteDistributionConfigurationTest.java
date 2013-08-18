package ch.cyberduck.core.gstorage;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class GoogleStorageWebsiteDistributionConfigurationTest extends AbstractTestCase {

    @Test
    public void testGetMethods() throws Exception {
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(new GoogleStorageSession(
                new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname())));
        assertEquals(Arrays.asList(Distribution.WEBSITE), configuration.getMethods(new Path("test.cyberduck.ch", Path.VOLUME_TYPE)));
    }

    @Test
    public void testGetProtocol() throws Exception {
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(new GoogleStorageSession(
                new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname())));
        assertEquals(new GoogleStorageProtocol(), configuration.getProtocol());
    }

    @Test
    public void testRead() throws Exception {
        final Host host = new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                properties.getProperty("google.projectid"), null
        ));
        final GoogleStorageSession session = new GoogleStorageSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                if(user.equals("Google OAuth2 Access Token")) {
                    return properties.getProperty("google.accesstoken");
                }
                if(user.equals("Google OAuth2 Refresh Token")) {
                    return properties.getProperty("google.refreshtoken");
                }
                return null;
            }
        }, new DisabledLoginController());
        assertTrue(session.isSecured());
        final DistributionConfiguration configuration
                = new GoogleStorageWebsiteDistributionConfiguration(session);
        final Distribution website = configuration.read(new Path("test.cyberduck.ch", Path.VOLUME_TYPE), Distribution.WEBSITE,
                new DisabledLoginController());
        assertTrue(website.isEnabled());
    }
}
