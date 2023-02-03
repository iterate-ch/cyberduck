package ch.cyberduck.core;

import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.s3.S3Protocol;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class HostParserTest {

    @Test
    public void testParseS3Scheme() throws HostParserException {
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(new TestS3Protocol()))).get("s3:/bucketname/key");
        assertEquals("s3.amazonaws.com", host.getHostname());
        assertEquals(Protocol.Type.s3, host.getProtocol().getType());
        assertEquals("/bucketname/key", host.getDefaultPath());
    }

    @Test
    public void testParseS3SchemeAccessKey() throws HostParserException {
        assertEquals(0, new Host(new S3Protocol(), "s3.amazonaws.com", 443, "cyberduck-test/key", new Credentials("AWS456", null))
                .compareTo(new HostParser(new ProtocolFactory(Collections.singleton(new TestS3Protocol()))).get("s3:AWS456@cyberduck-test/key")));
        assertEquals(0, new Host(new S3Protocol(), "s3.amazonaws.com", 443, "/cyberduck-test/key", new Credentials("AWS456", null))
                .compareTo(new HostParser(new ProtocolFactory(Collections.singleton(new TestS3Protocol()))).get("s3://AWS456@/cyberduck-test/key")));
    }

    @Test
    public void testParseBuckets() throws Exception {
        assertEquals(0, new Host(new S3Protocol(), "bucketname.s3.amazonaws.com", 443, "/bucket/key")
                .compareTo(new HostParser(new ProtocolFactory(Collections.singleton(new TestS3Protocol()))).get("s3://bucketname.s3.amazonaws.com/bucket/key")));
        assertEquals(0, new Host(new S3Protocol(), "s3.amazonaws.com", 443, "/bucket/key")
                .compareTo(new HostParser(new ProtocolFactory(Collections.singleton(new TestS3Protocol()))).get("s3:/bucket/key")));
    }

    private static class TestS3Protocol extends S3Protocol {
        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getDefaultHostname() {
            return "s3.amazonaws.com";
        }
    }
}
