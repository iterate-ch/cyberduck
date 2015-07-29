package ch.cyberduck.core.s3;

import ch.cyberduck.core.AbstractTestCase;
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
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;

import org.junit.Test;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class S3WriteFeatureTest extends AbstractTestCase {

    @Test
    public void testAppendBelowLimit() throws Exception {
        final S3Session session = new S3Session(new Host("h"));
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
        final S3Session session = new S3Session(new Host("h"));
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
                properties.getProperty("s3.key"), properties.getProperty("s3.secret")
        ));
        final S3Session session = new S3Session(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertFalse(new S3WriteFeature(session).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), Long.MAX_VALUE, PathCache.empty()).append);
        assertEquals(Write.notfound, new S3WriteFeature(session).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), Long.MAX_VALUE, PathCache.empty()));
        assertEquals(Write.notfound, new S3WriteFeature(session).append(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), 0L, PathCache.empty()));
        session.close();
    }
}
