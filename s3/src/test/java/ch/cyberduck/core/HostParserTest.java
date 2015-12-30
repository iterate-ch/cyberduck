package ch.cyberduck.core;

import ch.cyberduck.core.s3.S3Protocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class HostParserTest extends AbstractTestCase {

    @Test
    public void testParseS3Scheme() throws Exception {
        final Host host = new HostParser(new S3Protocol()).get("s3://bucketname/key");
        assertEquals("s3.amazonaws.com", host.getHostname());
        assertEquals(Protocol.Type.s3, host.getProtocol().getType());
        assertEquals("/bucketname/key", host.getDefaultPath());
    }

    @Test
    public void testParseS3SchemeAccessKey() throws Exception {
        assertTrue(new Host(new S3Protocol(), "s3.amazonaws.com", 443, "/cyberduck-test/key", new Credentials("AWS456", null))
                .compareTo(new HostParser(new S3Protocol()).get("s3://AWS456@cyberduck-test/key")) == 0);
    }
}
