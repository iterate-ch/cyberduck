package ch.cyberduck.ui.growl;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;

/**
 * @version $Id:$
 */
public class GrowlTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        GrowlNative.register();
    }

    @Test
    public void testNotify() throws Exception {
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