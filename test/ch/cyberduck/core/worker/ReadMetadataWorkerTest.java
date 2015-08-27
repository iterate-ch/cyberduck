package ch.cyberduck.core.worker;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.test.NullSession;

import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @version $Id$
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
        assertTrue(worker.run(new NullSession(new Host(""))).isEmpty());
    }

    @Test
    public void testDifferent() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        files.add(new Path("a", EnumSet.of(Path.Type.file)));
        files.add(new Path("b", EnumSet.of(Path.Type.file)));
        files.add(new Path("c", EnumSet.of(Path.Type.file)));
        ReadMetadataWorker worker = new ReadMetadataWorker(files) {
            @Override
            public void cleanup(final Map<String, String> result) {
                fail();
            }
        };
        final Map<String, String> map = worker.run(new NullSession(new Host("")));
        assertFalse(map.containsKey("key1"));
        assertFalse(map.containsKey("key2"));
    }

    @Test
    public void testRun() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        files.add(new Path("a", EnumSet.of(Path.Type.file)));
        files.add(new Path("b", EnumSet.of(Path.Type.file)));
        files.add(new Path("c", EnumSet.of(Path.Type.file)));
        ReadMetadataWorker worker = new ReadMetadataWorker(files) {
            @Override
            public void cleanup(final Map<String, String> result) {
                fail();
            }
        };
        final Map<String, String> map = worker.run(new NullSession(new Host("")));
        assertFalse(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
        assertNull(map.get("key2"));
        assertNotNull(map.get("key3"));
    }
}