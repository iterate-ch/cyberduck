package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;

import org.jets3t.service.model.StorageBucket;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
        session.open();
        final List<StorageBucket> list = new S3BucketListService().list(session);
        final S3Path container = new S3Path(session, "test.cyberduck.ch", Path.VOLUME_TYPE);
    }
}
