package ch.cyberduck.core.worker;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class WriteMetadataWorkerTest {

    @Test
    public void testRunEmpty() throws Exception {
        final List<Path> files = new ArrayList<>();
        WriteMetadataWorker worker = new WriteMetadataWorker(files, Collections.emptyMap(), false, ProgressListener.noop) {
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
                        public Map<String, String> getDefault(final Path file) {
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

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, ProgressListener.noop) {
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
                        public Map<String, String> getDefault(final Path file) {
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

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, ProgressListener.noop) {
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
                        public Map<String, String> getDefault(final Path file) {
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

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, ProgressListener.noop) {
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
                        public Map<String, String> getDefault(final Path file) {
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
        final PathAttributes attributesA = new
                DefaultPathAttributes();
        {
            final Map<String, String> map = new HashMap<>();
            map.put("equal", "equal");
            map.put("different", "diff1");
            map.put("unique", "unique");
            attributesA.setMetadata(map);
        }
        final PathAttributes attributesB = new DefaultPathAttributes();
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

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, ProgressListener.noop) {
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
                        public Map<String, String> getDefault(final Path file) {
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

    @Test
    public void testToUpdateWithoutCachedMetadata() {
        // Metadata not included in listing (S3, Swift)
        final Map<String, String> online = new HashMap<>();
        online.put("Content-Type", "text/plain");
        online.put("Cache-Control", "no-cache");
        final Map<String, String> expected = new HashMap<>(online);
        expected.put("user_tags", "Red%0A6");
        assertEquals(expected, WriteMetadataWorker.toUpdate(Collections.emptyMap(),
                Collections.singletonMap("user_tags", "Red%0A6"), online));
    }

    @Test
    public void testToUpdateChange() {
        final Map<String, String> cached = new HashMap<>();
        cached.put("Content-Type", "text/plain");
        cached.put("user_tags", "Work");
        final Map<String, String> metadata = new HashMap<>(cached);
        metadata.put("user_tags", "Red%0A6");
        final Map<String, String> expected = new HashMap<>(cached);
        expected.put("user_tags", "Red%0A6");
        assertEquals(expected, WriteMetadataWorker.toUpdate(cached, metadata, cached));
    }

    @Test
    public void testToUpdateRemove() {
        final Map<String, String> cached = new HashMap<>();
        cached.put("Content-Type", "text/plain");
        cached.put("user_tags", "Work");
        assertEquals(Collections.singletonMap("Content-Type", "text/plain"), WriteMetadataWorker.toUpdate(cached,
                Collections.singletonMap("Content-Type", "text/plain"), cached));
    }

    @Test
    public void testToUpdateRetainChangedOnServer() {
        // Entries changed on server since last known metadata are not overwritten
        final Map<String, String> cached = new HashMap<>();
        cached.put("Content-Type", "text/plain");
        final Map<String, String> online = new HashMap<>();
        online.put("Content-Type", "text/html");
        online.put("Cache-Control", "no-cache");
        final Map<String, String> metadata = new HashMap<>(cached);
        metadata.put("user_tags", "Work");
        final Map<String, String> expected = new HashMap<>(online);
        expected.put("user_tags", "Work");
        assertEquals(expected, WriteMetadataWorker.toUpdate(cached, metadata, online));
    }

    @Test
    public void testToUpdateUnchanged() {
        final Map<String, String> cached = Collections.singletonMap("user_tags", "Work");
        assertEquals(cached, WriteMetadataWorker.toUpdate(cached, cached, cached));
    }

    @Test
    public void testToUpdateNullValueKept() {
        // Null value for entries with different values in multiple files
        final Map<String, String> cached = Collections.singletonMap("key", "v1");
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("key", null);
        assertEquals(cached, WriteMetadataWorker.toUpdate(cached, metadata, cached));
    }

    @Test
    public void testToUpdateRemovedWithoutCachedMetadata() {
        // Contents of folder with metadata not included in listing
        final Map<String, String> online = new HashMap<>();
        online.put("A", "1");
        online.put("B", "2");
        assertEquals(Collections.singletonMap("A", "1"), WriteMetadataWorker.toUpdate(Collections.emptyMap(),
                Collections.singletonMap("A", "1"), online, Collections.singleton("B")));
        // Remove all
        assertEquals(Collections.emptyMap(), WriteMetadataWorker.toUpdate(Collections.emptyMap(),
                Collections.emptyMap(), online, new HashSet<>(Arrays.asList("A", "B"))));
        // Not removed without key removed from selection
        assertEquals(online, WriteMetadataWorker.toUpdate(Collections.emptyMap(),
                Collections.singletonMap("A", "1"), online, Collections.emptySet()));
    }

    @Test
    public void testToRemoved() {
        final Path a = new Path("/a", EnumSet.of(Path.Type.file));
        final Map<String, String> ma = new HashMap<>();
        ma.put("A", "1");
        ma.put("B", "2");
        a.attributes().setMetadata(ma);
        final Path b = new Path("/b", EnumSet.of(Path.Type.file));
        b.attributes().setMetadata(Collections.singletonMap("C", "3"));
        final Map<String, String> requested = new HashMap<>();
        requested.put("A", "1");
        requested.put("C", null);
        assertEquals(Collections.singleton("B"), WriteMetadataWorker.toRemoved(Arrays.asList(a, b), requested));
    }

    @Test
    public void testRunRecursiveRemoveFromChildWithoutCachedMetadata() throws Exception {
        final Path folder = new Path("/f", EnumSet.of(Path.Type.directory));
        final Map<String, String> selected = new HashMap<>();
        selected.put("A", "1");
        selected.put("B", "2");
        folder.attributes().setMetadata(selected);
        final Path child = new Path(folder, "c", EnumSet.of(Path.Type.file));
        final Map<String, Map<String, String>> written = new HashMap<>();
        new WriteMetadataWorker(Collections.singletonList(folder), Collections.singletonMap("A", "1"), true, ProgressListener.noop)
                .run(new NullSession(new Host(new TestProtocol())) {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T _getFeature(final Class<T> type) {
                        if(type == Metadata.class) {
                            return (T) new Metadata() {
                                @Override
                                public Map<String, String> getDefault(final Path file) {
                                    return Collections.emptyMap();
                                }

                                @Override
                                public Map<String, String> getMetadata(final Path file) {
                                    // Metadata on server for child
                                    final Map<String, String> online = new HashMap<>();
                                    online.put("A", "1");
                                    online.put("B", "2");
                                    online.put("Content-Type", "text/plain");
                                    return online;
                                }

                                @Override
                                public void setMetadata(final Path file, final TransferStatus status) {
                                    written.put(file.getName(), status.getMetadata());
                                }
                            };
                        }
                        if(type == ListService.class) {
                            // Listing without metadata
                            return (T) (ListService) (directory, listener) -> new AttributedList<>(Collections.singletonList(child));
                        }
                        return super._getFeature(type);
                    }
                });
        assertEquals(Collections.singletonMap("A", "1"), written.get("f"));
        final Map<String, String> expected = new HashMap<>();
        expected.put("A", "1");
        expected.put("Content-Type", "text/plain");
        assertEquals(expected, written.get("c"));
    }
}
