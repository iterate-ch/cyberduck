package ch.cyberduck.core.worker;

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
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

public class WriteMetadataWorkerTest {

    @Test
    public void testEmpty() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        WriteMetadataWorker worker = new WriteMetadataWorker(files, Collections.emptyMap(), false, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean result) {
                fail();
            }
        };
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type == Headers.class) {
                    return (T) new Headers() {
                        @Override
                        public Map<String, String> getDefault(final Local local) {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) throws BackgroundException {
                            fail();
                            return null;
                        }

                        @Override
                        public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
                            fail();
                        }
                    };
                }
                return super.getFeature(type);
            }
        });
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

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean map) {
                fail();
            }
        };
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type == Headers.class) {
                    return (T) new Headers() {
                        @Override
                        public Map<String, String> getDefault(final Local local) {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(final Path file) throws BackgroundException {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
                            fail();
                        }
                    };
                }
                return super.getFeature(type);
            }
        });
    }

    @Test
    public void testRun() throws Exception {
        final List<Path> files = new ArrayList<Path>();
        final Path p = new Path("a", EnumSet.of(Path.Type.file));
        files.add(p);
        final Map<String, String> previous = new HashMap<String, String>();
        previous.put("nullified", "hash");
        previous.put("key", "v1");
        p.attributes().setMetadata(previous);
        final Map<String, String> updated = new HashMap<String, String>();
        updated.put("nullified", null);
        updated.put("key", "v2");

        WriteMetadataWorker worker = new WriteMetadataWorker(files, updated, false, new DisabledProgressListener()) {
            @Override
            public void cleanup(final Boolean map) {
                fail();
            }
        };
        worker.run(new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type == Headers.class) {
                    return (T) new Headers() {
                        @Override
                        public Map<String, String> getDefault(final Local local) {
                            return Collections.emptyMap();
                        }

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
                    };
                }
                return super.getFeature(type);
            }
        });
    }
}
