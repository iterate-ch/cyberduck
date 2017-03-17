package ch.cyberduck.core.worker;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DeleteWorkerTest {

    @Test
    public void testCompile() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Delete.class) {
                    return (T) new Delete() {
                        @Override
                        public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
                            assertEquals(new Path("/t/a", EnumSet.of(Path.Type.file)), files.get(0));
                            assertEquals(new Path("/t/d/b", EnumSet.of(Path.Type.file)), files.get(1));
                            assertEquals(new Path("/t/d", EnumSet.of(Path.Type.directory)), files.get(2));
                            assertEquals(new Path("/t", EnumSet.of(Path.Type.directory)), files.get(3));
                        }

                        @Override
                        public boolean isRecursive() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
                return (T) super._getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                if(file.equals(new Path("/t", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<Path>(Arrays.asList(
                            new Path("/t/a", EnumSet.of(Path.Type.file)),
                            new Path("/t/d", EnumSet.of(Path.Type.directory))
                    ));
                }
                if(file.equals(new Path("/t/d", EnumSet.of(Path.Type.directory)))) {
                    return new AttributedList<Path>(Arrays.asList(
                            new Path("/t/d/b", EnumSet.of(Path.Type.file))
                    ));
                }
                fail();
                return null;
            }
        };
        final DeleteWorker worker = new DeleteWorker(new DisabledLoginCallback(),
                Collections.singletonList(new Path("/t", EnumSet.of(Path.Type.directory))),
                PathCache.empty(), new DisabledProgressListener());
        assertEquals(4, worker.run(session).size());
    }

    @Test
    public void testSymlink() throws Exception {
        final Session session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                return (T) new Delete() {
                    @Override
                    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
                        assertEquals(new Path("/s", EnumSet.of(Path.Type.directory, AbstractPath.Type.symboliclink)), files.get(0));
                    }

                    @Override
                    public boolean isRecursive() {
                        return false;
                    }
                };
            }

            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                fail();
                return null;
            }
        };
        final DeleteWorker worker = new DeleteWorker(new DisabledLoginCallback(),
                Collections.singletonList(new Path("/s", EnumSet.of(Path.Type.directory, AbstractPath.Type.symboliclink))),
                PathCache.empty(), new DisabledProgressListener());
        worker.run(session);
    }
}
