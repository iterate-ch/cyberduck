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
}
