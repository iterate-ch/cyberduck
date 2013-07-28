package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class HostUrlProviderTest extends AbstractTestCase {

    @Test
    public void testToUrl() {
        assertEquals("sftp://user@localhost", new HostUrlProvider().get(new Host(Protocol.SFTP, "localhost", new Credentials("user", "p") {
            @Override
            public String getUsernamePlaceholder() {
                return null;
            }

            @Override
            public String getPasswordPlaceholder() {
                return null;
            }
        })));
        assertEquals("sftp://localhost", new HostUrlProvider(false).get(new Host(Protocol.SFTP, "localhost", new Credentials("user", "p") {
            @Override
            public String getUsernamePlaceholder() {
                return null;
            }

            @Override
            public String getPasswordPlaceholder() {
                return null;
            }
        })));
        assertEquals("sftp://localhost:222",
                new HostUrlProvider(false).get(new Host(Protocol.SFTP, "localhost", 222)));
    }

}
