package ch.cyberduck.core.editor;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.NullLocal;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class LaunchServicesApplicationFinderTest extends AbstractTestCase {

    @Override
    @Before
    public void register() {
        super.register();
        LaunchServicesApplicationFinder.register();
    }

    @Test
    public void testFindAll() throws Exception {
        ApplicationFinder f = ApplicationFinderFactory.instance();
        final List<String> applications = f.findAll(new NullLocal(null, "file.txt"));
        assertFalse(applications.isEmpty());
        assertTrue(applications.contains("com.apple.TextEdit"));
        assertTrue(applications.contains("com.macromates.textmate"));
    }

    @Test
    public void testFind() throws Exception {
        ApplicationFinder f = ApplicationFinderFactory.instance();
        assertEquals("com.apple.Preview", f.find(new NullLocal(null, "file.png")));
    }

    @Test
    public void testGetName() throws Exception {
        ApplicationFinder f = ApplicationFinderFactory.instance();
        assertEquals("TextEdit", f.getName("com.apple.TextEdit"));
    }
}
