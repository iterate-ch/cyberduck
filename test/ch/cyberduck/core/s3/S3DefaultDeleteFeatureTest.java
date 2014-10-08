package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.gstorage.GoogleStorageProtocol;
import ch.cyberduck.core.gstorage.GoogleStorageSession;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test);
        assertTrue(new S3FindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController(), new DisabledProgressListener());
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }

    @Test
    public void testDeletePlaceholder() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new S3DirectoryFeature(session).mkdir(test, null);
        assertTrue(new S3FindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController(), new DisabledProgressListener());
        assertFalse(new S3FindFeature(session).find(test));
        session.close();
    }

    @Test
    public void testDeleteContainer() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("US");
        new S3DirectoryFeature(session).mkdir(container, null);
        assertTrue(new S3FindFeature(session).find(container));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginController(), new DisabledProgressListener());
        assertFalse(new S3FindFeature(session).find(container));
        session.close();
    }

    @Test
    public void testDeleteContainerGoogle() throws Exception {
        final GoogleStorageSession session = new GoogleStorageSession(new Host(new GoogleStorageProtocol(), new GoogleStorageProtocol().getDefaultHostname(), new Credentials(
                properties.getProperty("google.projectid"), null
        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore() {
            @Override
            public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                if(user.equals("Google OAuth2 Access Token")) {
                    return properties.getProperty("google.accesstoken");
                }
                if(user.equals("Google OAuth2 Refresh Token")) {
                    return properties.getProperty("google.refreshtoken");
                }
                return null;
            }
        }, new DisabledLoginController(), new DisabledCancelCallback());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume, Path.Type.directory));
        container.attributes().setRegion("US");
        new S3DirectoryFeature(session).mkdir(container, null);
        assertTrue(new S3FindFeature(session).find(container));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginController(), new DisabledProgressListener());
        assertFalse(new S3FindFeature(session).find(container));
        session.close();
    }

    @Ignore
    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundKey() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginController(), new DisabledProgressListener());
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundBucket() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginController(), new DisabledCancelCallback());
        final Path container = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.volume));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginController(), new DisabledProgressListener());
    }
}