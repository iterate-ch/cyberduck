package ch.cyberduck.core.editor;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.local.DisabledApplicationQuitCallback;
import ch.cyberduck.core.test.NullSession;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.AbstractController;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class BrowserBackgroundEditorTest extends AbstractTestCase {

    @Test
    public void testOpen() throws Exception {
        final AtomicBoolean t = new AtomicBoolean();
        final NullSession session = new NullSession(new Host("d")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type.equals(Read.class)) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
                            t.set(true);
                            return IOUtils.toInputStream("content");
                        }

                        @Override
                        public boolean append(final Path file) {
                            assertEquals(new Path("/f", EnumSet.of(Path.Type.file)), file);
                            return false;
                        }
                    };
                }
                if(type.equals(UrlProvider.class)) {
                    return (T) new UrlProvider() {
                        @Override
                        public DescriptiveUrlBag toUrl(final Path file) {
                            return new DescriptiveUrlBag();
                        }
                    };
                }
                return super.getFeature(type);
            }
        };
        final AtomicBoolean e = new AtomicBoolean();
        final Path file = new Path("/f", EnumSet.of(Path.Type.file));
        file.attributes().setSize("content".getBytes().length);
        final BrowserBackgroundEditor editor = new BrowserBackgroundEditor(new AbstractController() {
            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                //
            }

            @Override
            public <T> Future<T> background(final BackgroundAction<T> action) {
                final T run;
                try {
                    run = action.run();
                    assertTrue((Boolean) run);
                }
                catch(BackgroundException e) {
                    fail();
                    return null;
                }
                return ConcurrentUtils.constantFuture(run);
            }
        }, session, new Application("com.editor"), file) {
            @Override
            protected void edit(final ApplicationQuitCallback quit) throws IOException {
                e.set(true);
            }

            @Override
            protected void watch(final Local local) throws IOException {
                //
            }
        };
        editor.open(new DisabledApplicationQuitCallback(), new DisabledTransferErrorCallback());
        assertTrue(t.get());
        assertNotNull(editor.getLocal());
        assertTrue(e.get());
        assertTrue(editor.getLocal().exists());
    }
}
