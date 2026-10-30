package ch.cyberduck.core.worker;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DeleteWorkerTest {

    @Test
    public void testCompileDefault() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Delete.class) {
                    return (T) new Delete() {
                        @Override
                        public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) {
                            assertEquals(new Path("/t/a", EnumSet.of(Path.Type.file)), new ArrayList<>(files.keySet()).get(0));
                            assertEquals(new Path("/t/d/b", EnumSet.of(Path.Type.file)), new ArrayList<>(files.keySet()).get(1));
                            assertEquals(new Path("/t/d", EnumSet.of(Path.Type.directory)), new ArrayList<>(files.keySet()).get(2));
                            assertEquals(new Path("/t", EnumSet.of(Path.Type.directory)), new ArrayList<>(files.keySet()).get(3));
                        }
                    };
                }
                return super._getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(new Path("/t", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<>(Arrays.asList(
                            new Path("/t/a", EnumSet.of(Path.Type.file)),
                            new Path("/t/d", EnumSet.of(Path.Type.directory))
                    ));
                }
                if(file.equals(new Path("/t/d", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<>(Collections.singletonList(
                            new Path("/t/d/b", EnumSet.of(Path.Type.file))
                    ));
                }
                fail();
                return null;
            }
        };
        final DeleteWorker worker = new DeleteWorker(LoginCallback.noop,
                Collections.singletonList(new Path("/t", EnumSet.of(Path.Type.directory))),
                ProgressListener.noop);
        int hashCode = worker.hashCode();
        assertEquals(4, worker.run(session).size());
        assertEquals(hashCode, worker.hashCode());
    }

    @Test
    public void testCompileRecursiveDeleteSupported() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Delete.class) {
                    return (T) new Delete() {
                        @Override
                        public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) {
                            assertEquals(1, files.size());
                            assertEquals(new Path("/t", EnumSet.of(Path.Type.directory)), new ArrayList<>(files.keySet()).get(0));
                        }

                        @Override
                        public EnumSet<Flags> features(final Path file) {
                            return EnumSet.of(Flags.recursive);
                        }
                    };
                }
                return super._getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(new Path("/t", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<>(Arrays.asList(
                            new Path("/t/a", EnumSet.of(Path.Type.file)),
                            new Path("/t/d", EnumSet.of(Path.Type.directory))
                    ));
                }
                if(file.equals(new Path("/t/d", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<>(Collections.singletonList(
                            new Path("/t/d/b", EnumSet.of(Path.Type.file))
                    ));
                }
                fail();
                return null;
            }
        };
        final DeleteWorker worker = new DeleteWorker(LoginCallback.noop,
            Collections.singletonList(new Path("/t", EnumSet.of(Path.Type.directory))),
                ProgressListener.noop);
        assertEquals(1, worker.run(session).size());
    }

    @Test
    public void testCompileRecursiveDeleteSupportedMultipleFilesAsArgument() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Delete.class) {
                    return (T) new Delete() {
                        @Override
                        public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) {
                            assertEquals(1, files.size());
                            assertEquals(new Path("/t", EnumSet.of(Path.Type.directory)), new ArrayList<>(files.keySet()).get(0));
                        }

                        @Override
                        public EnumSet<Flags> features(final Path file) {
                            return EnumSet.of(Flags.recursive);
                        }
                    };
                }
                return super._getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(new Path("/t", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<>(Arrays.asList(
                            new Path("/t/a", EnumSet.of(Path.Type.file)),
                            new Path("/t/d", EnumSet.of(Path.Type.directory))
                    ));
                }
                if(file.equals(new Path("/t/d", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<>(Collections.singletonList(
                            new Path("/t/d/b", EnumSet.of(Path.Type.file))
                    ));
                }
                fail();
                return null;
            }
        };
        final DeleteWorker worker = new DeleteWorker(LoginCallback.noop,
            Arrays.asList(
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new Path("/t/a", EnumSet.of(Path.Type.file)),
                new Path("/t/d", EnumSet.of(Path.Type.directory)),
                new Path("/t/d", EnumSet.of(Path.Type.directory)),
                new Path("/t/d/b", EnumSet.of(Path.Type.file))
            ),
                ProgressListener.noop);
        assertEquals(1, worker.run(session).size());
    }

    @Test
    public void testSymlink() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Delete.class) {
                    return (T) new Delete() {
                        @Override
                        public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) {
                            assertEquals(new Path("/s", EnumSet.of(Path.Type.directory, AbstractPath.Type.symboliclink)), new ArrayList<>(files.keySet()).get(0));
                        }
                    };
                }
                return super._getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                fail();
                return null;
            }
        };
        final DeleteWorker worker = new DeleteWorker(LoginCallback.noop,
            Collections.singletonList(new Path("/s", EnumSet.of(Path.Type.directory, AbstractPath.Type.symboliclink))),
                ProgressListener.noop);
        worker.run(session);
    }

    @Test
    public void testEvictCachedListingOfDeletedFolders() throws Exception {
        final Path root = new Path("/", EnumSet.of(Path.Type.directory));
        final Path folder = new Path("/t", EnumSet.of(Path.Type.directory));
        final Path subfolder = new Path("/t/d", EnumSet.of(Path.Type.directory));
        final Path other = new Path("/o", EnumSet.of(Path.Type.directory));
        final PathCache cache = new PathCache(10);
        cache.put(root, new AttributedList<>(Arrays.asList(folder, other)));
        cache.put(folder, new AttributedList<>(Arrays.asList(new Path("/t/a", EnumSet.of(Path.Type.file)), subfolder)));
        cache.put(subfolder, new AttributedList<>(Collections.singletonList(new Path("/t/d/b", EnumSet.of(Path.Type.file)))));
        cache.put(other, new AttributedList<>(Collections.singletonList(new Path("/o/c", EnumSet.of(Path.Type.file)))));
        final Session session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Delete.class) {
                    return (T) new Delete() {
                        @Override
                        public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) {
                            //
                        }

                        @Override
                        public EnumSet<Flags> features(final Path file) {
                            return EnumSet.of(Flags.recursive);
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        new DeleteWorker(LoginCallback.noop, Collections.singletonList(folder), ProgressListener.noop, cache, false).run(session);
        assertFalse(cache.isCached(folder));
        assertFalse(cache.isCached(subfolder));
        assertTrue(cache.isCached(other));
    }

    @Test
    public void testEvictCachedListingOfDeletedFoldersOnFailure() throws Exception {
        final Path root = new Path("/", EnumSet.of(Path.Type.directory));
        final Path folder = new Path("/t", EnumSet.of(Path.Type.directory));
        final Path subfolder = new Path("/t/d", EnumSet.of(Path.Type.directory));
        final Path other = new Path("/o", EnumSet.of(Path.Type.directory));
        final PathCache cache = new PathCache(10);
        cache.put(root, new AttributedList<>(Arrays.asList(folder, other)));
        cache.put(folder, new AttributedList<>(Collections.singletonList(subfolder)));
        cache.put(subfolder, new AttributedList<>(Collections.singletonList(new Path("/t/d/b", EnumSet.of(Path.Type.file)))));
        cache.put(other, new AttributedList<>(Collections.singletonList(new Path("/o/c", EnumSet.of(Path.Type.file)))));
        final Session session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Delete.class) {
                    return (T) new Delete() {
                        @Override
                        public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
                            for(Path file : files.keySet()) {
                                if(file.equals(other)) {
                                    // Fail after first folder has already been deleted
                                    throw new AccessDeniedException(file.getAbsolute());
                                }
                                callback.delete(file);
                            }
                        }

                        @Override
                        public EnumSet<Flags> features(final Path file) {
                            return EnumSet.of(Flags.recursive);
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        try {
            new DeleteWorker(LoginCallback.noop, Arrays.asList(folder, other), ProgressListener.noop, cache, false).run(session);
            fail();
        }
        catch(AccessDeniedException e) {
            // Expected
        }
        assertFalse(cache.isCached(folder));
        assertFalse(cache.isCached(subfolder));
        assertTrue(cache.isCached(root));
    }
}
