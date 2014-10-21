package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultAttributesFeature;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.iterate.openstack.swift.Client;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftFindFeatureTest extends AbstractTestCase {

    @Test
    public void testFindContainer() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        assertTrue(new SwiftFindFeature(session).find(container));
        session.close();
    }

    @Test
    public void testFindKey() throws Exception {
        // Test with and without region
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertFalse(new SwiftFindFeature(session).find(file));
        try {
            new DefaultAttributesFeature(session).find(file);
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        new SwiftTouchFeature(session).touch(file);
        assertTrue(new SwiftFindFeature(session).find(file));
        assertNotNull(new DefaultAttributesFeature(session).find(file));
        session.close();
    }

    @Test
    public void testFindRoot() throws Exception {
        assertTrue(new SwiftFindFeature(new SwiftSession(new Host("h"))).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testNoCacheNotFound() throws Exception {
        final Cache cache = new Cache();
        final AttributedList<Path> list = AttributedList.emptyList();
        cache.put(new Path("/g", EnumSet.of(Path.Type.directory)).getReference(), list);
        final AtomicBoolean b = new AtomicBoolean();
        final Find finder = new SwiftFindFeature(new SwiftMetadataFeature(new SwiftSession(new Host("t")) {
            @Override
            public Client getClient() {
                fail();
                return null;
            }
        }) {
            @Override
            public Map<String, String> getMetadata(final Path file) throws BackgroundException {
                b.set(true);
                return Collections.emptyMap();
            }
        }).withCache(cache);
        assertTrue(finder.find(new Path("/g/gd", EnumSet.of(Path.Type.file))));
        assertTrue(b.get());
    }

    @Test
    public void testCacheNotFound() throws Exception {
        final Cache cache = new Cache();
        final AttributedList<Path> list = AttributedList.emptyList();
        list.attributes().addHidden(new Path("/g/gd", EnumSet.of(Path.Type.file)));
        cache.put(new Path("/g", EnumSet.of(Path.Type.directory)).getReference(), list);
        final Find finder = new SwiftFindFeature(new SwiftMetadataFeature(new SwiftSession(new Host("t")) {
            @Override
            public Client getClient() {
                fail();
                return null;
            }
        }) {
            @Override
            public Map<String, String> getMetadata(final Path file) throws BackgroundException {
                fail();
                return null;
            }
        }).withCache(cache);
        assertFalse(finder.find(new Path("/g/gd", EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testCacheFound() throws Exception {
        final Cache cache = new Cache();
        final AttributedList<Path> list = new AttributedList<Path>(Collections.singletonList(new Path("/g/gd", EnumSet.of(Path.Type.file))));
        cache.put(new Path("/g", EnumSet.of(Path.Type.directory)).getReference(), list);
        final Find finder = new SwiftFindFeature(new SwiftMetadataFeature(new SwiftSession(new Host("t")) {
            @Override
            public Client getClient() {
                fail();
                return null;
            }
        }) {
            @Override
            public Map<String, String> getMetadata(final Path file) throws BackgroundException {
                fail();
                return null;
            }
        }).withCache(cache);
        assertTrue(finder.find(new Path("/g/gd", EnumSet.of(Path.Type.file))));
    }
}