package ch.cyberduck.core.s3;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
@Category(IntegrationTest.class)
public class S3WriteFeatureTest {

    @Test
    public void testAppendBelowLimit() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol()));
        final S3WriteFeature feature = new S3WriteFeature(session, null, new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                return true;
            }

            @Override
            public Find withCache(final PathCache cache) {
                return this;
            }
        }, new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                return new PathAttributes();
            }

            @Override
            public Attributes withCache(final PathCache cache) {
                return this;
            }
        });
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), 0L, PathCache.empty());
        assertFalse(append.append);
    }

    @Test
    public void testSize() throws Exception {
        final S3Session session = new S3Session(new Host(new S3Protocol()));
        final S3WriteFeature feature = new S3WriteFeature(session, null, new Find() {
            @Override
            public boolean find(final Path file) throws BackgroundException {
                return true;
            }

            @Override
            public Find withCache(final PathCache cache) {
                return this;
            }
        }, new Attributes() {
            @Override
            public PathAttributes find(final Path file) throws BackgroundException {
                final PathAttributes attributes = new PathAttributes();
                attributes.setSize(3L);
                return attributes;
            }

            @Override
            public Attributes withCache(final PathCache cache) {
                return this;
            }
        });
        final Write.Append append = feature.append(new Path("/p", EnumSet.of(Path.Type.file)), 0L, PathCache.empty());
        assertFalse(append.append);
        assertTrue(append.override);
        assertEquals(3L, append.size, 0L);
    }

    @Test
    public void testAppendNoMultipartFound() throws Exception {
        final Host host = new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(), new Credentials(
                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertFalse(new S3WriteFeature(session).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), Long.MAX_VALUE, PathCache.empty()).append);
        assertEquals(Write.notfound, new S3WriteFeature(session).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), Long.MAX_VALUE, PathCache.empty()));
        assertEquals(Write.notfound, new S3WriteFeature(session).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 0L, PathCache.empty()));
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteChunkedTransferAWS2SignatureFailure() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS2);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3WriteFeature feature = new S3WriteFeature(session);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        try {
            feature.write(file, status);
        }
        finally {
            session.close();
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteChunkedTransferAWS4Signature() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.AWS4HMACSHA256);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final S3WriteFeature feature = new S3WriteFeature(session);
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomStringUtils.random(5 * 1024 * 1024).getBytes("UTF-8");
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content)));
        try {
            feature.write(file, status);
        }
        catch(InteroperabilityException e) {
            assertEquals("A header you provided implies functionality that is not implemented. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
        finally {
            session.close();
        }
    }
}
