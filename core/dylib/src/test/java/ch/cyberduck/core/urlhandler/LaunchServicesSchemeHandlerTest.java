package ch.cyberduck.core.urlhandler;

import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

@Ignore
public class LaunchServicesSchemeHandlerTest {

    @Test
    public void testSetDefaultHandlerForURLScheme() {
        final SchemeHandler l = new LaunchServicesSchemeHandler(new LaunchServicesApplicationFinder());
        l.setDefaultHandler(
            new Application("none.app", null), Collections.singletonList(Scheme.ftp.name())
        );
        assertEquals(new Application("com.apple.finder"), l.getDefaultHandler(Scheme.ftp.name()));
        assertFalse(l.isDefaultHandler(Collections.singletonList(Scheme.ftp.name()), new Application("other.app", null)));
        l.setDefaultHandler(
            new Application("ch.sudo.cyberduck", null), Collections.singletonList(Scheme.ftp.name())
        );
        assertEquals("ch.sudo.cyberduck", l.getDefaultHandler(Scheme.ftp.name()).getIdentifier());
        assertTrue(l.getAllHandlers(Scheme.ftp.name()).contains(new Application("ch.sudo.cyberduck")));
        assertNotSame("ch.sudo.cyberduck", l.getDefaultHandler(Scheme.http.name()).getIdentifier());
        assertTrue(l.isDefaultHandler(Collections.singletonList(Scheme.ftp.name()), new Application("ch.sudo.cyberduck", null)));
    }
}
