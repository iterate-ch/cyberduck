package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class S3BucketListServiceTest extends AbstractTestCase {

    @Test
    public void testGetContainer() throws Exception {
        assertEquals("bucketname", new S3BucketListService().getContainer(new Host("bucketname.s3.amazonaws.com")));
    }
}
