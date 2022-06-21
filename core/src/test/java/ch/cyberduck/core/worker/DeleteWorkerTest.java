package ch.cyberduck.core.worker;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
        final DeleteWorker worker = new DeleteWorker(new DisabledLoginCallback(),
            Collections.singletonList(new Path("/t", EnumSet.of(Path.Type.directory))),
                new DisabledProgressListener());
        assertEquals(4, worker.run(session).size());
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
                        public boolean isRecursive() {
                            return true;
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
        final DeleteWorker worker = new DeleteWorker(new DisabledLoginCallback(),
            Collections.singletonList(new Path("/t", EnumSet.of(Path.Type.directory))),
                new DisabledProgressListener());
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
                        public boolean isRecursive() {
                            return true;
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
        final DeleteWorker worker = new DeleteWorker(new DisabledLoginCallback(),
            Arrays.asList(
                new Path("/t", EnumSet.of(Path.Type.directory)),
                new Path("/t/a", EnumSet.of(Path.Type.file)),
                new Path("/t/d", EnumSet.of(Path.Type.directory)),
                new Path("/t/d", EnumSet.of(Path.Type.directory)),
                new Path("/t/d/b", EnumSet.of(Path.Type.file))
            ),
                new DisabledProgressListener());
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

                        @Override
                        public boolean isRecursive() {
                            return false;
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
        final DeleteWorker worker = new DeleteWorker(new DisabledLoginCallback(),
            Collections.singletonList(new Path("/s", EnumSet.of(Path.Type.directory, AbstractPath.Type.symboliclink))),
                new DisabledProgressListener());
        worker.run(session);
    }
}
