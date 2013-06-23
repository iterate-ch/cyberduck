package ch.cyberduck.ui.growl;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Test;

import java.util.concurrent.Callable;

/**
 * @version $Id:$
 */
public class NotificationCenterTest extends AbstractTestCase {

    @Test
    public void testNotify() throws Exception {
        NotificationCenter.register();
        final Growl growl = GrowlFactory.get();
        this.repeat(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                growl.notify("title", "test");
                return null;
            }
        }, 20);
    }
}
