package ch.cyberduck.core.shared;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.sftp.SFTPHomeDirectoryService;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.sftp.SFTPSession;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DefaultAttributesFeatureTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final SFTPSession session = new SFTPSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        new DefaultAttributesFeature(session).find(new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testAttributes() throws Exception {
        final Host host = new Host(new SFTPProtocol(), "test.cyberduck.ch", new Credentials(
                System.getProperties().getProperty("sftp.user"), System.getProperties().getProperty("sftp.password")
        ));
        final AtomicBoolean set = new AtomicBoolean();
        final SFTPSession session = new SFTPSession(host) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
                assertFalse(set.get());
                final AttributedList<Path> list = super.list(file, listener);
                set.set(true);
                return list;
            }
        };
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final PathCache cache = new PathCache(1);
        final DefaultAttributesFeature f = new DefaultAttributesFeature(session).withCache(cache);
        final Path workdir = new SFTPHomeDirectoryService(session).find();
        final Path file = new Path(workdir, "test", EnumSet.of(Path.Type.file));
        final Attributes attributes = f.find(file);
        assertEquals(0L, attributes.getSize());
        assertEquals("1106", attributes.getOwner());
        assertEquals(new Permission("-rw-rw-rw-"), attributes.getPermission());
        // Test cache
        assertEquals(0L, f.find(file).getSize());
        assertTrue(cache.containsKey(file.getParent()));
        // Test wrong type
        try {
            f.find(new Path(workdir, "test", EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        session.close();
    }

    @Test
    public void testFindPlaceholder() throws Exception {
        assertNotNull(new DefaultAttributesFeature(new NullSession(new Host(new TestProtocol())) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                return new AttributedList<>(Collections.singletonList(new Path("/a/b", EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
            }
        }).find(new Path("/a/b", EnumSet.of(Path.Type.directory))));
    }
}
