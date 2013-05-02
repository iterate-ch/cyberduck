package ch.cyberduck.core.editor;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.NullPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.MainAction;
import ch.cyberduck.ui.Controller;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class WatchEditorTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        WatchEditorFactory.register();
    }

    @Test
    public void testEdit() throws Exception {
        final NullPath path = new NullPath("/f1/f2/t", Path.FILE_TYPE);
        path.attributes().setDuplicate(true);
        path.attributes().setVersionId("1");
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
        }, new Application("com.apple.TextEdit", null), path);
        assertEquals(new Application("com.apple.TextEdit", null), ((AbstractEditor) e).getApplication());
        assertEquals("t", ((AbstractEditor) e).getEdited().getName());
        final Local local = ((AbstractEditor) e).getEdited().getLocal();
        assertEquals("t-1", local.getName());
        assertEquals("f2", local.getParent().getName());
        assertEquals("f1", local.getParent().getParent().getName());
        assertEquals(path.getHost().getUuid(), local.getParent().getParent().getParent().getName());
    }
}