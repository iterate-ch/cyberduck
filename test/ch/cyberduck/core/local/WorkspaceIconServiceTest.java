package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.application.NSImage;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class WorkspaceIconServiceTest extends AbstractTestCase {

    @Before
    @Override
    public void register() {
        super.register();
        WorkspaceIconService.register();
    }

    @Test
    public void testSetProgress() throws Exception {
        final WorkspaceIconService s = (WorkspaceIconService) IconServiceFactory.instance();
        final Callable<Local> c = new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                final NullLocal file = new NullLocal(Preferences.instance().getProperty("tmp.dir"), UUID.randomUUID().toString()) {
                    @Override
                    public void setIcon(final int progress) {
                        //
                    }
                };
                assertFalse(s.setIcon(file, NSImage.imageWithContentsOfFile("img/download0.icns")));
                file.touch();
                assertTrue(s.setIcon(file, NSImage.imageWithContentsOfFile("img/download0.icns")));
                file.delete();
                return file;
            }
        };
        // Test concurrency as set icon is not thread safe
        final ExecutorService service = Executors.newCachedThreadPool();
        final BlockingQueue<Future<Local>> queue = new LinkedBlockingQueue<Future<Local>>();
        final CompletionService<Local> completion = new ExecutorCompletionService<Local>(service, queue);
        int repeat = 50;
        for(int i = 0; i < repeat; i++) {
            completion.submit(c);
        }
        for(int i = 0; i < repeat; i++) {
            queue.take().get();
        }
    }
}