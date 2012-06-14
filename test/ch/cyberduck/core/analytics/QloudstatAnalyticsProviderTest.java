package ch.cyberduck.core.analytics;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Protocol;

import org.junit.Assert;
import org.junit.Test;

/**
 * @version $Id:$
 */
public class QloudstatAnalyticsProviderTest extends AbstractTestCase {

    @Test
    public void testGetSetup() throws Exception {
        QloudstatAnalyticsProvider q = new QloudstatAnalyticsProvider();
        Assert.assertEquals("https://qloudstat.com/account/register?setup=cHJvdmlkZXI9czMuYW1hem9uYXdzLmNvbSxlbmRwb2ludD1jeWJlcmR1Y2stdGVzdGluZyxrZXk9cWxvdWRzdGF0LHNlY3JldD1zZWNyZXQ%3D", q.getSetup(Protocol.S3_SSL, "cyberduck-testing", new Credentials("qloudstat", "secret") {
            @Override
            public String getUsernamePlaceholder() {
                return Protocol.S3_SSL.getUsernamePlaceholder();
            }

            @Override
            public String getPasswordPlaceholder() {
                return Protocol.S3_SSL.getPasswordPlaceholder();
            }
        }));
    }
}
