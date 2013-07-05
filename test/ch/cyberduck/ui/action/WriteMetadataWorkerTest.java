package ch.cyberduck.ui.action;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Metadata;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class WriteMetadataWorkerTest extends AbstractTestCase {

    @Test
    public void testEmpty() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        WriteMetadataWorker worker = new WriteMetadataWorker(new NullSession(new Host("h")), files, Collections.<String, String>emptyMap()) {
            @Override
            public void cleanup(final Map<String, String> result) {
                fail();
            }
        };
        assertTrue(worker.run().isEmpty());
    }

    @Test
    public void testEqual() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        final NullPath p = new NullPath("a", Path.FILE_TYPE);
        final Map<String, String> previous = new HashMap<String, String>();
        previous.put("key", "v1");
        p.attributes().setMetadata(previous);
        files.add(p);
        final Map<String, String> updated = new HashMap<String, String>();
        updated.put("key", "v1");
        final NullSession session = new NullSession(new Host("h")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                return (T) new Metadata() {
                    @Override
                    public Map<String, String> get(final Path file) throws BackgroundException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void write(final Path file, final Map<String, String> metadata) throws BackgroundException {
                        fail();
                    }
                };
            }
        };
        WriteMetadataWorker worker = new WriteMetadataWorker(session, files, updated) {
            @Override
            public void cleanup(final Map<String, String> map) {
                fail();
            }
        };
        assertEquals(updated, worker.run());
    }

    @Test
    public void testRun() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        final NullPath p = new NullPath("a", Path.FILE_TYPE);
        final Map<String, String> previous = new HashMap<String, String>();
        previous.put("nullified", "hash");
        previous.put("key", "v1");
        p.attributes().setMetadata(previous);
        files.add(p);
        final Map<String, String> updated = new HashMap<String, String>();
        updated.put("nullified", null);
        updated.put("key", "v2");
        final NullSession session = new NullSession(new Host("h")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                return (T) new Metadata() {
                    @Override
                    public Map<String, String> get(final Path file) throws BackgroundException {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void write(final Path file, final Map<String, String> meta) throws BackgroundException {
                        assertTrue(meta.containsKey("nullified"));
                        assertTrue(meta.containsKey("key"));
                        assertEquals("v2", meta.get("key"));
                        assertEquals("hash", meta.get("nullified"));
                    }
                };
            }
        };
        WriteMetadataWorker worker = new WriteMetadataWorker(session, files, updated) {
            @Override
            public void cleanup(final Map<String, String> map) {
                fail();
            }
        };
        assertEquals(updated, worker.run());
    }
}
