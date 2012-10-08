package ch.cyberduck.ui.action;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class WriteMetadataWorkerTest extends AbstractTestCase {

    @Test
    public void testEmpty() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        WriteMetadataWorker worker = new WriteMetadataWorker(files, Collections.<String, String>emptyMap()) {
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
        final NullPath p = new NullPath("a", Path.FILE_TYPE) {
            @Override
            public void writeMetadata(final Map<String, String> meta) {
                fail();
            }
        };
        final Map<String, String> previous = new HashMap<String, String>();
        previous.put("key", "v1");
        p.attributes().setMetadata(previous);
        files.add(p);
        final Map<String, String> updated = new HashMap<String, String>();
        updated.put("key", "v1");
        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated) {
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
        final NullPath p = new NullPath("a", Path.FILE_TYPE) {
            @Override
            public void writeMetadata(final Map<String, String> meta) {
                assertTrue(meta.containsKey("nullified"));
                assertTrue(meta.containsKey("key"));
                assertEquals("v2", meta.get("key"));
                assertEquals("hash", meta.get("nullified"));
            }
        };
        final Map<String, String> previous = new HashMap<String, String>();
        previous.put("nullified", "hash");
        previous.put("key", "v1");
        p.attributes().setMetadata(previous);
        files.add(p);
        final Map<String, String> updated = new HashMap<String, String>();
        updated.put("nullified", null);
        updated.put("key", "v2");
        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated) {
            @Override
            public void cleanup(final Map<String, String> map) {
                fail();
            }
        };
        assertEquals(updated, worker.run());
    }
}
