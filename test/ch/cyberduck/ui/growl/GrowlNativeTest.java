package ch.cyberduck.ui.growl;

import ch.cyberduck.core.AbstractTestCase;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.Callable;

/**
 * @version $Id$
 */
public class GrowlNativeTest extends AbstractTestCase {

    @Test
    @Ignore
    public void testNotify() throws Exception {
        final Growl growl = new GrowlNative();
        this.repeat(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                growl.notify("title", "test");
                return null;
            }
        }, 20);
    }
}