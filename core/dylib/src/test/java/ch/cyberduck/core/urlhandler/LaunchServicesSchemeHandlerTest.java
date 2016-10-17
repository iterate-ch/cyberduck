package ch.cyberduck.core.urlhandler;

import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class LaunchServicesSchemeHandlerTest {

    @Test
    public void testSetDefaultHandlerForURLScheme() throws Exception {
        final SchemeHandler l = new LaunchServicesSchemeHandler(new LaunchServicesApplicationFinder());
        l.setDefaultHandler(
                Arrays.asList(Scheme.ftp), new Application("none.app", null)
        );
        assertEquals(Application.notfound, l.getDefaultHandler(Scheme.ftp));
        assertFalse(l.isDefaultHandler(Arrays.asList(Scheme.ftp), new Application("other.app", null)));
        l.setDefaultHandler(
                Arrays.asList(Scheme.ftp), new Application("ch.sudo.cyberduck", null)
        );
        assertEquals("ch.sudo.cyberduck", l.getDefaultHandler(Scheme.ftp).getIdentifier());
        assertNotSame("ch.sudo.cyberduck", l.getDefaultHandler(Scheme.http).getIdentifier());
        assertTrue(l.isDefaultHandler(Arrays.asList(Scheme.ftp), new Application("ch.sudo.cyberduck", null)));
    }
}
