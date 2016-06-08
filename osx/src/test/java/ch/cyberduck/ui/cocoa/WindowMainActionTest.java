package ch.cyberduck.ui.cocoa;

import ch.cyberduck.binding.WindowController;
import ch.cyberduck.core.threading.WindowMainAction;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @version $Id$
 */
public class WindowMainActionTest {

    @Test
    public void testIsValid() throws Exception {
        assertFalse(new WindowMainAction(new WindowController() {
            @Override
            protected String getBundleName() {
                return null;
            }
        }) {

            @Override
            public void run() {
                //
            }
        }.isValid());
    }
}
