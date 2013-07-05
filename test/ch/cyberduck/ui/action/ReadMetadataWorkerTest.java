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

/**
 * @version $Id$
 */
public class ReadMetadataWorkerTest extends AbstractTestCase {

    @Test
    public void testEmpty() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        ReadMetadataWorker worker = new ReadMetadataWorker(new NullSession(new Host("h")), files) {
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
        files.add(new NullPath("a", Path.FILE_TYPE));
        files.add(new NullPath("b", Path.FILE_TYPE));
        files.add(new NullPath("c", Path.FILE_TYPE));
        final NullSession session = new NullSession(new Host("h")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                return (T) new Metadata() {
                    @Override
                    public Map<String, String> get(final Path file) throws BackgroundException {
                        if(file.getName().equals("a")) {
                            return Collections.singletonMap("key1", "value1");
                        }
                        else if(file.getName().equals("b")) {
                            return Collections.singletonMap("key2", "value2");
                        }
                        else if(file.getName().equals("c")) {
                            return Collections.singletonMap("key2", "value2");
                        }
                        else {
                            fail();
                        }
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void write(final Path file, final Map<String, String> metadata) throws BackgroundException {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        ReadMetadataWorker worker = new ReadMetadataWorker(session, files) {
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
        files.add(new NullPath("a", Path.FILE_TYPE));
        files.add(new NullPath("b", Path.FILE_TYPE));
        files.add(new NullPath("c", Path.FILE_TYPE));
        final NullSession session = new NullSession(new Host("h")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                return (T) new Metadata() {
                    @Override
                    public Map<String, String> get(final Path file) throws BackgroundException {
                        final HashMap<String, String> map = new HashMap<String, String>();
                        if(file.getName().equals("a")) {
                            map.put("key1", "v1");
                            map.put("key2", "v");
                            map.put("key3", "v");
                            return map;
                        }
                        else if(file.getName().equals("b")) {
                            map.put("key2", "v");
                            map.put("key3", "v");
                            return map;
                        }
                        else if(file.getName().equals("c")) {
                            map.put("key2", "v2");
                            map.put("key3", "v");
                            return map;
                        }
                        else {
                            fail();
                        }
                        return map;
                    }

                    @Override
                    public void write(final Path file, final Map<String, String> metadata) throws BackgroundException {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        ReadMetadataWorker worker = new ReadMetadataWorker(session, files) {
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