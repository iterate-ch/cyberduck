package ch.cyberduck.core.urlhandler;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.editor.Application;
import ch.cyberduck.core.editor.LaunchServicesApplicationFinder;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @version $Id:$
 */
public class LaunchServicesSchemeHandlerTest extends AbstractTestCase {

    @BeforeClass
    public static void register() {
        LaunchServicesApplicationFinder.register();
        LaunchServicesSchemeHandler.register();
    }

    @Test
    public void testSetDefaultHandlerForURLScheme() throws Exception {
        final SchemeHandler l = SchemeHandlerFactory.instance();
        l.setDefaultHandler(
                Arrays.asList(Scheme.ftp), new Application("none.app", null)
        );
        assertEquals("none.app", l.getDefaultHandler(Scheme.ftp).getIdentifier());
        assertFalse(l.isDefaultHandler(Arrays.asList(Scheme.ftp), new Application("other.app", null)));
        l.setDefaultHandler(
                Arrays.asList(Scheme.ftp), new Application("ch.sudo.cyberduck", null)
        );
        assertEquals("ch.sudo.cyberduck", l.getDefaultHandler(Scheme.ftp).getIdentifier());
        assertNotSame("ch.sudo.cyberduck", l.getDefaultHandler(Scheme.http).getIdentifier());
        assertTrue(l.isDefaultHandler(Arrays.asList(Scheme.ftp), new Application("ch.sudo.cyberduck", null)));
    }
}
