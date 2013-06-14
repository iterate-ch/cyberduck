package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.AbstractLoginController;
import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCanceledException;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.sftp.SFTPPath;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.core.threading.BackgroundException;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @version $Id:$
 */
public class CustomOriginCloudFrontDistributionConfigurationTest extends AbstractTestCase {

    @Test
    public void testGetMethods() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertEquals(Arrays.asList(Distribution.CUSTOM),
                new CustomOriginCloudFrontDistributionConfiguration(session, new AbstractLoginController() {
                    @Override
                    public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason, final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
                        throw new LoginCanceledException();
                    }
                }).getMethods(new SFTPPath(new SFTPSession(new Host("h")), "/bbb", Path.VOLUME_TYPE)));
    }

    @Test
    public void testGetOrigin() throws Exception {
        final Host h = new Host("m");
        final SFTPPath container = new SFTPPath(new SFTPSession(h), "/", Path.VOLUME_TYPE);
        h.setWebURL("http://w.example.net");
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        final CustomOriginCloudFrontDistributionConfiguration configuration = new CustomOriginCloudFrontDistributionConfiguration(
                session, new AbstractLoginController() {
            @Override
            public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason, final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
                throw new LoginCanceledException();
            }
        });
        assertEquals("w.example.net", configuration.getOrigin(container, Distribution.CUSTOM));
        h.setWebURL(null);
        assertEquals("m", configuration.getOrigin(container, Distribution.CUSTOM));
    }


    @Test
    public void testRead() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname());
        host.setCredentials(new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        final CustomOriginCloudFrontDistributionConfiguration configuration = new CustomOriginCloudFrontDistributionConfiguration(session, new AbstractLoginController() {
            @Override
            public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason, final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
                throw new LoginCanceledException();
            }
        });
        final SFTPPath container = new SFTPPath(new SFTPSession(new Host(Protocol.SFTP, "myhost.localdomain")), "test.cyberduck.ch", Path.VOLUME_TYPE);
        final Distribution distribution = configuration.read(container, Distribution.CUSTOM);
        assertNull(distribution.getId());
        assertEquals("myhost.localdomain", distribution.getOrigin());
        assertEquals("Unknown", distribution.getStatus());
        assertEquals(null, distribution.getId());
    }

    @Test(expected = BackgroundException.class)
    public void testReadMissingCredentials() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname());
        final S3Session session = new S3Session(host);
        final Host bookmark = new Host(Protocol.SFTP, "myhost.localdomain");
        final CustomOriginCloudFrontDistributionConfiguration configuration = new CustomOriginCloudFrontDistributionConfiguration(session, new AbstractLoginController() {
            @Override
            public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason, final boolean enableKeychain, final boolean enablePublicKey, final boolean enableAnonymous) throws LoginCanceledException {
                assertEquals(Protocol.S3_SSL, protocol);
                assertEquals(session.getHost().getCredentials(), credentials);
                assertEquals(true, enableKeychain);
                assertEquals(false, enableAnonymous);
                assertEquals(false, enablePublicKey);
                assertEquals("No login credentials could be found in the Keychain.", reason);
                throw new LoginCanceledException();
            }
        });
        final SFTPPath container = new SFTPPath(new SFTPSession(bookmark), "test.cyberduck.ch", Path.VOLUME_TYPE);
        try {
            configuration.read(container, Distribution.CUSTOM);
        }
        catch(BackgroundException e) {
            assertEquals(LoginCanceledException.class, e.getCause().getClass());
            throw e;
        }
    }

}
