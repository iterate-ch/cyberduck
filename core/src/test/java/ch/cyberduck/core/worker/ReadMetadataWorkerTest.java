package ch.cyberduck.core.worker;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
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
public class ReadMetadataWorkerTest {

    @Test
    public void testEmpty() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        ReadMetadataWorker worker = new ReadMetadataWorker(files) {
            @Override
            public void cleanup(final Map<String, String> result) {
                fail();
            }
        };
        assertTrue(worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Headers.class) {
                    return (T) new Headers() {
                        @Override
                        public Map<String, String> getDefault() {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super.getFeature(type);
            }
        }).isEmpty());
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
        final Map<String, String> map = worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Headers.class) {
                    return (T) new Headers() {
                        @Override
                        public Map<String, String> getDefault() {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) throws BackgroundException {
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
                        public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super.getFeature(type);
            }
        });
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
        final Map<String, String> map = worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Headers.class) {
                    return (T) new Headers() {
                        @Override
                        public Map<String, String> getDefault() {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) throws BackgroundException {
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
                        public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super.getFeature(type);
            }
        });
        assertFalse(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
        assertNull(map.get("key2"));
        assertNotNull(map.get("key3"));
    }
}