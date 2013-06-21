package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class S3SessionTest extends AbstractTestCase {

    @Test
    public void testFile() {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, "h"));
        assertFalse(session.isCreateFileSupported(new S3Path(session, null, "/", Path.VOLUME_TYPE)));
        assertTrue(session.isCreateFileSupported(new S3Path(session, new S3Path(session, null, "/", Path.VOLUME_TYPE), "/container", Path.VOLUME_TYPE)));
    }

    @Test
    public void testConnect() throws Exception {
        final S3Session session = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
    }

    @Test
    public void testFeatures() throws Exception {
        final S3Session aws = new S3Session(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname()));
        assertTrue(aws.isVersioningSupported());
        assertTrue(aws.isAnalyticsSupported());
        assertTrue(aws.isChecksumSupported());
        assertTrue(aws.isLifecycleSupported());
        assertTrue(aws.isLocationSupported());
        assertTrue(aws.isRevertSupported());
        final S3Session o = new S3Session(new Host(Protocol.S3_SSL, "o"));
        assertFalse(aws.isVersioningSupported());
        assertFalse(aws.isAnalyticsSupported());
        assertTrue(aws.isChecksumSupported());
        assertFalse(aws.isLifecycleSupported());
        assertFalse(aws.isLocationSupported());
        assertFalse(aws.isRevertSupported());
    }
}
