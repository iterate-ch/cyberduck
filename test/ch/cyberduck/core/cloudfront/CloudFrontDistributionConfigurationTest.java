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
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.s3.S3Session;

import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.model.cloudfront.LoggingStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class CloudFrontDistributionConfigurationTest extends AbstractTestCase {

    @Test
    public void testGetMethods() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertEquals(Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING),
                new CloudFrontDistributionConfiguration(session, new DisabledLoginController()).getMethods(new Path("/bbb", Path.VOLUME_TYPE)));
    }

    @Test
    public void testGetName() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        final DistributionConfiguration configuration = new CloudFrontDistributionConfiguration(
                session, new DisabledLoginController()
        );
        assertEquals("Amazon CloudFront", configuration.getName());
        assertEquals("Amazon CloudFront", configuration.getName(Distribution.CUSTOM));
    }

    @Test
    public void testGetOrigin() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        final CloudFrontDistributionConfiguration configuration
                = new CloudFrontDistributionConfiguration(session, new DisabledLoginController());
        assertEquals("bbb.s3.amazonaws.com",
                configuration.getOrigin(new Path("/bbb", Path.VOLUME_TYPE), Distribution.DOWNLOAD));
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
        final DistributionConfiguration configuration
                = new CloudFrontDistributionConfiguration(session, new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Distribution distribution = configuration.read(container, Distribution.DOWNLOAD);
        assertEquals("E2N9XG26504TZI", distribution.getId());
    }

    @Test(expected = LoginCanceledException.class)
    public void testReadLoginFailure() throws Exception {
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname());
        final S3Session session = new S3Session(host);
        final DistributionConfiguration configuration
                = new CloudFrontDistributionConfiguration(session, new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        configuration.read(container, Distribution.DOWNLOAD);
    }

    @Test
    public void testWriteExists() throws Exception {
        final AtomicBoolean set = new AtomicBoolean();
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname());
        host.setCredentials(new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final DistributionConfiguration configuration = new CloudFrontDistributionConfiguration(session, new DisabledLoginController()) {
            @Override
            protected void updateDistribution(final Distribution current, final CloudFrontService client, final Path container, final Distribution distribution, final LoggingStatus logging) throws CloudFrontServiceException, IOException, ConnectionCanceledException {
                set.set(true);
            }

            @Override
            protected org.jets3t.service.model.cloudfront.Distribution createDistribution(final CloudFrontService client, final Path container, final Distribution distribution, final LoggingStatus logging) throws ConnectionCanceledException, CloudFrontServiceException {
                fail();
                return null;
            }
        };
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        configuration.write(container, new Distribution("test.cyberduck.ch.s3.amazonaws.com", Distribution.DOWNLOAD));
        assertTrue(set.get());
    }

    @Test
    public void testWriteNew() throws Exception {
        final AtomicBoolean set = new AtomicBoolean();
        final Host host = new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname());
        host.setCredentials(new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final DistributionConfiguration configuration = new CloudFrontDistributionConfiguration(session, new DisabledLoginController()) {
            @Override
            protected void updateDistribution(final Distribution current, final CloudFrontService client, final Path container, final Distribution distribution, final LoggingStatus logging) throws CloudFrontServiceException, IOException, ConnectionCanceledException {
                fail();
            }

            @Override
            protected org.jets3t.service.model.cloudfront.Distribution createDistribution(final CloudFrontService client, final Path container, final Distribution distribution, final LoggingStatus logging) throws ConnectionCanceledException, CloudFrontServiceException {
                set.set(true);
                return null;
            }
        };
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        configuration.write(container, new Distribution("test.cyberduck.ch.s3.amazonaws.com", Distribution.STREAMING));
        assertTrue(set.get());
    }

    @Test
    public void testProtocol() {
        assertEquals("cloudfront.amazonaws.com", new CloudFrontDistributionConfiguration(
                new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname())), new DisabledLoginController()
        ).getProtocol().getDefaultHostname());
    }
}
