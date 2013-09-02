package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id:$
 */
public class S3MoveFeatureTest extends AbstractTestCase {

    @Test
    public void testMove() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new S3TouchFeature(session).touch(test);
        final Path renamed = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        assertTrue(session.exists(test));
        new S3MoveFeature(session).move(test, renamed);
        assertFalse(session.exists(test));
        assertTrue(session.exists(renamed));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(renamed), new DisabledLoginController());
        session.close();
    }
}
