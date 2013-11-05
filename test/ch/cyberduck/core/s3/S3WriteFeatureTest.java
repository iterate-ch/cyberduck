package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id:$
 */
public class S3WriteFeatureTest extends AbstractTestCase {

    @Test
    public void testAppendBelowLimit() throws Exception {
        assertFalse(new S3WriteFeature(null).append(new Path("/p", Path.FILE_TYPE), 0L, null).append);
    }

    @Test
    public void testAppendNoMultipartFound() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        assertFalse(new S3WriteFeature(session).append(new Path(container, "/p", Path.FILE_TYPE), 10L * 1024L * 1024L, null).append);
        session.close();
    }
}
