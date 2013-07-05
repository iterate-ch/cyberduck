package ch.cyberduck.core.cloudfront;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.sftp.SFTPPath;
import ch.cyberduck.core.sftp.SFTPSession;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class CloudFrontDistributionConfigurationTest extends AbstractTestCase {

    @Test
    public void testGetMethods() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertEquals(Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING),
                new CloudFrontDistributionConfiguration(session).getMethods(new SFTPPath(new SFTPSession(new Host(Protocol.SFTP, "h")), "/bbb", Path.VOLUME_TYPE)));
    }

    @Test
    public void testGetName() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        final DistributionConfiguration configuration = new CloudFrontDistributionConfiguration(
                session
        );
        assertEquals("Amazon CloudFront", configuration.getName());
        assertEquals("Amazon CloudFront", configuration.getName(Distribution.CUSTOM));
    }

    @Test
    public void testGetOrigin() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        final CloudFrontDistributionConfiguration configuration = new CloudFrontDistributionConfiguration(session);
        assertEquals("bbb.s3.amazonaws.com",
                configuration.getOrigin(new S3Path(session, "/bbb", Path.VOLUME_TYPE), Distribution.DOWNLOAD));
    }

    @Test
    public void testRead() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname());
        host.setCredentials(new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final DistributionConfiguration configuration = new CloudFrontDistributionConfiguration(session);
        final S3Path container = new S3Path(session, "test.cyberduck.ch", Path.VOLUME_TYPE);
        final Distribution distribution = configuration.read(container, Distribution.DOWNLOAD);
        assertEquals("E2N9XG26504TZI", distribution.getId());
    }

    @Test(expected = LoginFailureException.class)
    public void testReadLoginFailure() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname());
        final S3Session session = new S3Session(host);
        final DistributionConfiguration configuration = new CloudFrontDistributionConfiguration(session);
        final S3Path container = new S3Path(session, "test.cyberduck.ch", Path.VOLUME_TYPE);
        configuration.read(container, Distribution.DOWNLOAD);
    }
}
