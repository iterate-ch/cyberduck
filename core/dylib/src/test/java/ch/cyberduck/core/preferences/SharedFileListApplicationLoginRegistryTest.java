package ch.cyberduck.core.preferences;

import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.DisabledApplicationFinder;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.FinderSidebarService;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.SidebarService;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SharedFileListApplicationLoginRegistryTest {

    @Test
    public void testRegister() {
        final SharedFileListApplicationLoginRegistry registry = new SharedFileListApplicationLoginRegistry(new LaunchServicesApplicationFinder());
        final Application application = new Application("ch.sudo.cyberduck");
        assertTrue(registry.register(application));
        assertTrue(new FinderSidebarService(SidebarService.List.login).contains(new FinderLocal(NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(application.getIdentifier()))));
        assertTrue(registry.unregister(application));
    }

    @Test
    public void testRegisterNotInstalled() {
        final SharedFileListApplicationLoginRegistry registry = new SharedFileListApplicationLoginRegistry(new DisabledApplicationFinder());
        assertFalse(registry.register(new Application("ch.sudo.cyberduck")));
    }
}
