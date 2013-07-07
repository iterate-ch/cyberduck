package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3BucketListServiceTest extends AbstractTestCase {

    @Test
    public void testGetContainer() throws Exception {
        assertEquals("bucketname", new S3BucketListService().getContainer(new Host(Protocol.S3_SSL, "bucketname.s3.amazonaws.com")));
        assertEquals(null, new S3BucketListService().getContainer(new Host("bucketname.s3.amazonaws.com")));
    }

    @Test
    public void testList() throws Exception {
        final S3Session session = new S3Session(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        final List<Path> list = new S3BucketListService().list(session);
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new S3Path("test.cyberduck.ch", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE)));
    }
}
