package ch.cyberduck.core.analytics;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.cloudfront.CloudFrontDistributionConfiguration;
import ch.cyberduck.core.gstorage.GoogleStorageProtocol;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Assert;
import org.junit.Test;

/**
 * @version $Id$
 */
public class QloudstatAnalyticsProviderTest extends AbstractTestCase {

    @Test
    public void testGetSetupS3() {
        QloudstatAnalyticsProvider q = new QloudstatAnalyticsProvider();
        Assert.assertEquals("https://qloudstat.com/configuration/add?setup=cHJvdmlkZXI9czMuYW1hem9uYXdzLmNvbSxwcm90b2NvbD1odHRwLGVuZHBvaW50PWN5YmVyZHVjay10ZXN0aW5nLGtleT1xbG91ZHN0YXQsc2VjcmV0PXNlY3JldA%3D%3D",
                q.getSetup(new S3Protocol().getDefaultHostname(), Scheme.http, "cyberduck-testing", new Credentials("qloudstat", "secret")).getUrl());
    }

    @Test
    public void testGetSetupGoogleStorage() {
        QloudstatAnalyticsProvider q = new QloudstatAnalyticsProvider();
        Assert.assertEquals("https://qloudstat.com/configuration/add?setup=cHJvdmlkZXI9c3RvcmFnZS5nb29nbGVhcGlzLmNvbSxwcm90b2NvbD1odHRwLGVuZHBvaW50PXRlc3QuY3liZXJkdWNrLmNoLGtleT1xbG91ZHN0YXQsc2VjcmV0PXNlY3JldA%3D%3D",
                q.getSetup(new GoogleStorageProtocol().getDefaultHostname(), Scheme.http, "test.cyberduck.ch", new Credentials("qloudstat", "secret")).getUrl());
    }

    @Test
    public void testGetSetupRackspace() {
        QloudstatAnalyticsProvider q = new QloudstatAnalyticsProvider();
        Assert.assertEquals("https://qloudstat.com/configuration/add?setup=cHJvdmlkZXI9aWRlbnRpdHkuYXBpLnJhY2tzcGFjZWNsb3VkLmNvbSxwcm90b2NvbD1odHRwLGVuZHBvaW50PXRlc3QuY3liZXJkdWNrLmNoLGtleT1xbG91ZHN0YXQsc2VjcmV0PXNlY3JldA%3D%3D",
                q.getSetup("identity.api.rackspacecloud.com", Scheme.http, "test.cyberduck.ch", new Credentials("qloudstat", "secret")).getUrl());
    }

    @Test
    public void testGetSetupCloudFrontStreaming() {
        QloudstatAnalyticsProvider q = new QloudstatAnalyticsProvider();
        Assert.assertEquals("https://qloudstat.com/configuration/add?setup=cHJvdmlkZXI9Y2xvdWRmcm9udC5hbWF6b25hd3MuY29tLHByb3RvY29sPXJ0bXAsZW5kcG9pbnQ9Y3liZXJkdWNrLXRlc3Rpbmcsa2V5PXFsb3Vkc3RhdCxzZWNyZXQ9c2VjcmV0",
                q.getSetup(new CloudFrontDistributionConfiguration(
                        new S3Session(new Host(new S3Protocol(), new S3Protocol().getDefaultHostname())),
                        new DisabledX509TrustManager(), new DefaultX509KeyManager()
                ).getHostname(), Scheme.rtmp, "cyberduck-testing", new Credentials("qloudstat", "secret")).getUrl());
    }
}
