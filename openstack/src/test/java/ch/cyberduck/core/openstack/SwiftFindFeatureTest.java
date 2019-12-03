package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.iterate.openstack.swift.Client;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftFindFeatureTest extends AbstractSwiftTest {

    @Test
    public void testFindContainer() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        assertTrue(new SwiftFindFeature(session).find(container));
    }

    @Test
    public void testFindKey() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertFalse(new SwiftFindFeature(session).find(file));
        try {
            new DefaultAttributesFinderFeature(session).find(file);
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(file, new TransferStatus());
        assertTrue(new SwiftFindFeature(session).find(file));
        assertNotNull(new DefaultAttributesFinderFeature(session).find(file));
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new SwiftFindFeature(session).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testNoCacheNotFound() throws Exception {
        final PathCache cache = new PathCache(1);
        final AttributedList<Path> list = new AttributedList<Path>();
        cache.put(new Path("/g", EnumSet.of(Path.Type.directory)), list);
        final AtomicBoolean b = new AtomicBoolean();
        final Find finder = new SwiftFindFeature(new SwiftMetadataFeature(new SwiftSession(new Host(new SwiftProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            public Client getClient() {
                fail();
                return null;
            }
        }) {
            @Override
            public Map<String, String> getMetadata(final Path file) {
                b.set(true);
                return Collections.emptyMap();
            }
        }).withCache(cache);
        assertTrue(finder.find(new Path("/g/" + UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
        assertTrue(b.get());
    }
}
