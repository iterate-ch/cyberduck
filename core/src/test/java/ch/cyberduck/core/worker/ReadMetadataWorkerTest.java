package ch.cyberduck.core.worker;

import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ReadMetadataWorkerTest {

    @Test
    public void testEmpty() throws Exception {
        final List<Path> files = new ArrayList<>();
        ReadMetadataWorker worker = new ReadMetadataWorker(files) {
            @Override
            public void cleanup(final Map<String, String> result) {
                fail();
            }
        };
        assertTrue(worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Metadata.class) {
                    return (T) new Metadata() {
                        @Override
                        public Map<String, String> getDefault() {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setMetadata(final Path file, final TransferStatus status) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super._getFeature(type);
            }
        }).isEmpty());
    }

    @Test
    public void testDifferent() throws Exception {
        final List<Path> files = new ArrayList<>();
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
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Metadata.class) {
                    return (T) new Metadata() {
                        @Override
                        public Map<String, String> getDefault() {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) {
                            switch(file.getName()) {
                                case "a":
                                    return Collections.singletonMap("key1", "value1");
                                case "b":
                                    return Collections.singletonMap("key2", "value2");
                                case "c":
                                    return Collections.singletonMap("key2", "value2");
                                default:
                                    fail();
                                    break;
                            }
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setMetadata(final Path file, final TransferStatus status) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
        assertTrue(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
        assertNull(map.get("key1"));
        assertNull(map.get("key2"));
    }

    @Test
    public void testRun() throws Exception {
        final List<Path> files = new ArrayList<>();
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
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Metadata.class) {
                    return (T) new Metadata() {
                        @Override
                        public Map<String, String> getDefault() {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) {
                            final HashMap<String, String> map = new HashMap<>();
                            switch(file.getName()) {
                                case "a":
                                    map.put("key1", "v1");
                                    map.put("key2", "v");
                                    map.put("key3", "v");
                                    return map;
                                case "b":
                                    map.put("key2", "v");
                                    map.put("key3", "v");
                                    return map;
                                case "c":
                                    map.put("key2", "v2");
                                    map.put("key3", "v");
                                    return map;
                                default:
                                    fail();
                                    break;
                            }
                            return map;
                        }

                        @Override
                        public void setMetadata(final Path file, final TransferStatus status) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
        assertTrue(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
        assertNull(map.get("key1"));
        assertNull(map.get("key2"));
        assertNotNull(map.get("key3"));
    }
}
