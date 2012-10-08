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
public class ReadMetadataWorkerTest extends AbstractTestCase {

    @Test
    public void testEmpty() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        ReadMetadataWorker worker = new ReadMetadataWorker(files) {
            @Override
            public void cleanup(final Map<String, String> result) {
                fail();
            }
        };
        assertTrue(worker.run().isEmpty());
    }

    @Test
    public void testDifferent() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        files.add(new NullPath("a", Path.FILE_TYPE) {
            @Override
            public void readMetadata() {
                this.attributes().setMetadata(Collections.singletonMap("key1", "value1"));
            }
        });
        files.add(new NullPath("b", Path.FILE_TYPE) {
            @Override
            public void readMetadata() {
                this.attributes().setMetadata(Collections.singletonMap("key2", "value2"));
            }
        });
        files.add(new NullPath("c", Path.FILE_TYPE) {
            @Override
            public void readMetadata() {
                this.attributes().setMetadata(Collections.singletonMap("key2", "value2"));
            }
        });
        ReadMetadataWorker worker = new ReadMetadataWorker(files) {
            @Override
            public void cleanup(final Map<String, String> result) {
                fail();
            }
        };
        final Map<String, String> map = worker.run();
        assertFalse(map.containsKey("key1"));
        assertFalse(map.containsKey("key2"));
    }

    @Test
    public void testRun() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        files.add(new NullPath("a", Path.FILE_TYPE) {
            @Override
            public void readMetadata() {
                final HashMap<String, String> map = new HashMap<String, String>();
                map.put("key1", "v1");
                map.put("key2", "v");
                map.put("key3", "v");
                this.attributes().setMetadata(map);
            }
        });
        files.add(new NullPath("b", Path.FILE_TYPE) {
            @Override
            public void readMetadata() {
                final HashMap<String, String> map = new HashMap<String, String>();
                map.put("key2", "v");
                map.put("key3", "v");
                this.attributes().setMetadata(map);
            }
        });
        files.add(new NullPath("c", Path.FILE_TYPE) {
            @Override
            public void readMetadata() {
                final HashMap<String, String> map = new HashMap<String, String>();
                map.put("key2", "v2");
                map.put("key3", "v");
                this.attributes().setMetadata(map);
            }
        });
        ReadMetadataWorker worker = new ReadMetadataWorker(files) {
            @Override
            public void cleanup(final Map<String, String> result) {
                fail();
            }
        };
        final Map<String, String> map = worker.run();
        assertFalse(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
        assertNull(map.get("key2"));
        assertNotNull(map.get("key3"));
    }
}