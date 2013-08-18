package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class S3DefaultDeleteFeatureTest extends AbstractTestCase {

    @Test
    public void testDeleteFile() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new S3TouchFeature(session).touch(test);
        assertTrue(session.exists(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController());
        assertFalse(session.exists(test));
        session.close();
    }

    @Test
    public void testDeletePlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        new S3DirectoryFeature(session).mkdir(test, null);
        assertTrue(session.exists(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController());
        assertFalse(session.exists(test));
        session.close();
    }

    @Test
    public void testDeleteContainer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path(UUID.randomUUID().toString(), Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        new S3DirectoryFeature(session).mkdir(container, null);
        assertTrue(session.exists(container));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginController());
        assertFalse(session.exists(container));
        session.close();
    }

    @Ignore
    @Test(expected = NotfoundException.class)
    public void testDeleteNotFound() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController());
    }
}