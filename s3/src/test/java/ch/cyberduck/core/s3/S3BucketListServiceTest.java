package ch.cyberduck.core.s3;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3BucketListServiceTest extends AbstractS3Test {

    @Test
    public void testGetContainer() throws Exception {
        assertEquals("bucketname", new S3BucketListService(new S3Session(new Host(new S3Protocol()))).getContainer(new Host(new S3Protocol(), "bucketname.s3.amazonaws.com")));
        assertNull(new S3BucketListService(new S3Session(new Host(new S3Protocol()))).getContainer(new Host(new TestProtocol(), "bucketname.s3.amazonaws.com")));
    }

    @Test
    public void testList() throws Exception {
        final AttributedList<Path> list = new S3BucketListService(session).list(
            new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        session.close();
    }

    @Test
    public void testListWithRootDefaultPath() throws Exception {
        final AttributedList<Path> list = new S3BucketListService(session).list(
            new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        session.close();
    }

    @Test
    public void testListRestrictRegion() throws Exception {
        final AttributedList<Path> list = new S3BucketListService(session, new S3LocationFeature.S3Region("eu-central-1")).list(
            new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path bucket : list) {
            assertEquals("eu-central-1", bucket.attributes().getRegion());
        }
        session.close();
    }
}
