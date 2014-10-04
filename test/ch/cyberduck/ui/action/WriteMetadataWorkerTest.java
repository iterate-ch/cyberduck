package ch.cyberduck.ui.action;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class WriteMetadataWorkerTest extends AbstractTestCase {

    @Test
    public void testEmpty() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        WriteMetadataWorker worker = new WriteMetadataWorker(new Headers() {
            @Override
            public Map<String, String> getMetadata(final Path file) throws BackgroundException {
                fail();
                return null;
            }

            @Override
            public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
                fail();
            }
        }, files, Collections.<String, String>emptyMap(), new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean result) {
                fail();
            }
        };
        worker.run();
    }

    @Test
    public void testEqual() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        final Path p = new Path("a", EnumSet.of(Path.Type.file));
        final Map<String, String> previous = new HashMap<String, String>();
        previous.put("key", "v1");
        p.attributes().setMetadata(previous);
        files.add(p);
        final Map<String, String> updated = new HashMap<String, String>();
        updated.put("key", "v1");
        WriteMetadataWorker worker = new WriteMetadataWorker(new Headers() {
            @Override
            public Map<String, String> getMetadata(final Path file) throws BackgroundException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
                fail();
            }
        }, files, updated, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean map) {
                fail();
            }
        };
        worker.run();
    }

    @Test
    public void testRun() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        final Path p = new Path("a", EnumSet.of(Path.Type.file));
        final Map<String, String> previous = new HashMap<String, String>();
        previous.put("nullified", "hash");
        previous.put("key", "v1");
        p.attributes().setMetadata(previous);
        files.add(p);
        final Map<String, String> updated = new HashMap<String, String>();
        updated.put("nullified", null);
        updated.put("key", "v2");
        WriteMetadataWorker worker = new WriteMetadataWorker(new Headers() {
            @Override
            public Map<String, String> getMetadata(final Path file) throws BackgroundException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setMetadata(final Path file, final Map<String, String> meta) throws BackgroundException {
                assertTrue(meta.containsKey("nullified"));
                assertTrue(meta.containsKey("key"));
                assertEquals("v2", meta.get("key"));
                assertEquals("hash", meta.get("nullified"));
            }
        }, files, updated, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean map) {
                fail();
            }
        };
        worker.run();
    }
}
