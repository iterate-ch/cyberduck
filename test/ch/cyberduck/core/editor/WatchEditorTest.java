package ch.cyberduck.core.editor;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.ui.Controller;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class WatchEditorTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        LaunchServicesApplicationFinder.register();
        WatchEditorFactory.register();
    }

    @Test
    @Ignore
    public void testEdit() throws Exception {
        final Editor e = EditorFactory.instance().create(new Controller() {
            @Override
            public <T> Future<T> background(final BackgroundAction<T> runnable) {
                return null;
            }

            @Override
            public void invoke(final MainAction runnable) {
                //
            }

            @Override
            public void invoke(final MainAction runnable, final boolean wait) {
                //
            }

            @Override
            public boolean isMainThread() {
                return true;
            }
        }, new Application("com.apple.TextEdit", null), new NullPath("t", Path.FILE_TYPE));
        assertEquals(new Application("com.apple.TextEdit", null), ((AbstractEditor) e).getApplication());
        assertEquals(new NullPath("t", Path.FILE_TYPE), ((AbstractEditor) e).getEdited());
    }
}