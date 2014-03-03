package ch.cyberduck.ui.cocoa.quicklook;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class QuartzQuickLookTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        QuartzQuickLook.register();
    }

    @Test
    public void testSelect() throws Exception {
        QuickLook q = QuickLookFactory.get();
        final List<Local> files = new ArrayList<Local>();
        files.add(new NullLocal("f"));
        files.add(new NullLocal("b"));
        q.select(files);
    }

    @Test
    public void testIsAvailable() throws Exception {
        assertTrue(QuickLookFactory.get().isAvailable());
    }

    @Test
    public void testOpen() throws Exception {
        assertFalse(QuickLookFactory.get().isOpen());
    }
}