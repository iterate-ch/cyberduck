package ch.cyberduck.core.worker;

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class WriteMetadataWorkerTest {

    @Test
    public void testRunEmpty() throws Exception {
        final List<Path> files = new ArrayList<>();
        WriteMetadataWorker worker = new WriteMetadataWorker(files, Collections.emptyMap(), false, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean result) {
                fail();
            }
        };
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Metadata.class) {
                    return (T) new Metadata() {
                        @Override
                        public Map<String, String> getDefault(final Local local) {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) {
                            fail();
                            return null;
                        }

                        @Override
                        public void setMetadata(final Path file, final TransferStatus status) {
                            fail();
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
    }

    @Test
    public void testRunEqual() throws Exception {
        final List<Path> files = new ArrayList<>();
        final Path p = new Path("a", EnumSet.of(Path.Type.file));
        final Map<String, String> previous = new HashMap<>();
        previous.put("key", "v1");
        p.attributes().setMetadata(previous);
        files.add(p);
        final Map<String, String> updated = new HashMap<>();
        updated.put("key", "v1");

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean map) {
                fail();
            }
        };
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Metadata.class) {
                    return (T) new Metadata() {
                        @Override
                        public Map<String, String> getDefault(final Local local) {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setMetadata(final Path file, final TransferStatus status) {
                            fail();
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
    }

    @Test
    public void testRunUpdated() throws Exception {
        final List<Path> files = new ArrayList<>();
        final Path p = new Path("a", EnumSet.of(Path.Type.file));
        files.add(p);
        final Map<String, String> previous = new HashMap<>();
        previous.put("nullified", "hash");
        previous.put("key", "v1");
        p.attributes().setMetadata(previous);
        final Map<String, String> updated = new HashMap<>();
        updated.put("nullified", null);
        updated.put("key", "v2");

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean map) {
                fail();
            }
        };
        final AtomicBoolean call = new AtomicBoolean();
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Metadata.class) {
                    return (T) new Metadata() {
                        @Override
                        public Map<String, String> getDefault(final Local local) {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setMetadata(final Path file, final TransferStatus status) {
                            assertTrue(status.getMetadata().containsKey("nullified"));
                            assertTrue(status.getMetadata().containsKey("key"));
                            assertEquals("v2", status.getMetadata().get("key"));
                            assertEquals("hash", status.getMetadata().get("nullified"));
                            call.set(true);
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
        assertTrue(call.get());
    }

    @Test
    public void testRunAdd() throws Exception {
        final List<Path> files = new ArrayList<>();
        final Path p = new Path("a", EnumSet.of(Path.Type.file));
        files.add(p);
        final Map<String, String> previous = new HashMap<>();
        previous.put("k1", "v1");
        p.attributes().setMetadata(previous);
        final Map<String, String> updated = new HashMap<>();
        updated.put("k1", "v1");
        updated.put("k2", "v2");

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean map) {
                fail();
            }
        };
        final AtomicBoolean call = new AtomicBoolean();
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Metadata.class) {
                    return (T) new Metadata() {
                        @Override
                        public Map<String, String> getDefault(final Local local) {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setMetadata(final Path file, final TransferStatus status) {
                            assertTrue(status.getMetadata().containsKey("k1"));
                            assertTrue(status.getMetadata().containsKey("k2"));
                            assertEquals("v1", status.getMetadata().get("k1"));
                            assertEquals("v2", status.getMetadata().get("k2"));
                            call.set(true);
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
        assertTrue(call.get());
    }

    @Test
    public void testRunDifferent() throws Exception {
        final PathAttributes attributesA = new PathAttributes();
        {
            final Map<String, String> map = new HashMap<>();
            map.put("equal", "equal");
            map.put("different", "diff1");
            map.put("unique", "unique");
            attributesA.setMetadata(map);
        }
        final PathAttributes attributesB = new PathAttributes();
        {
            final Map<String, String> map = new HashMap<>();
            map.put("equal", "equal");
            map.put("different", "diff2");
            attributesB.setMetadata(map);
        }
        final List<Path> files = Arrays.asList(
            new Path("a", EnumSet.of(Path.Type.file),
                attributesA),
            new Path("b", EnumSet.of(Path.Type.file),
                attributesB));

        final Map<String, String> updated = new HashMap<>();
        updated.put("equal", "equal-changed");
        updated.put("unique", null);
        updated.put("different", null);

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean map) {
                fail();
            }
        };
        final AtomicBoolean call = new AtomicBoolean();
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Metadata.class) {
                    return (T) new Metadata() {
                        @Override
                        public Map<String, String> getDefault(final Local local) {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setMetadata(final Path file, final TransferStatus status) {
                            assertTrue(status.getMetadata().containsKey("equal"));
                            assertTrue(status.getMetadata().containsKey("different"));
                            assertEquals("equal-changed", status.getMetadata().get("equal"));

                            switch(file.getName()) {
                                case "a":
                                    assertTrue(status.getMetadata().containsKey("unique"));

                                    assertEquals("diff1", status.getMetadata().get("different"));
                                    assertEquals("unique", status.getMetadata().get("unique"));
                                    break;
                                case "b":
                                    assertFalse(status.getMetadata().containsKey("unique"));

                                    assertEquals("diff2", status.getMetadata().get("different"));
                                    break;
                                default:
                                    fail();
                                    break;
                            }
                            call.set(true);
                        }
                    };
                }
                return super._getFeature(type);
            }
        });
        assertTrue(call.get());
    }
}
