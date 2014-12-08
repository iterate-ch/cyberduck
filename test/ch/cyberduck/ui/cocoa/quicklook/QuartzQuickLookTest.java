package ch.cyberduck.ui.cocoa.quicklook;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.test.Depends;
import ch.cyberduck.core.test.NullLocal;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class QuartzQuickLookTest extends AbstractTestCase {

    @Test
    public void testSelect() throws Exception {
        QuickLook q = new QuartzQuickLook();
        final List<Local> files = new ArrayList<Local>();
        files.add(new NullLocal("f"));
        files.add(new NullLocal("b"));
        q.select(files);
    }

    @Test
    public void testIsAvailable() throws Exception {
        assertTrue(new QuartzQuickLook().isAvailable());
    }

    @Test
    public void testOpen() throws Exception {
        assertFalse(new QuartzQuickLook().isOpen());
    }
}