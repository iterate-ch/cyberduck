package ch.cyberduck.core.local;

import ch.cyberduck.core.Factory;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;

/**
 * @version $Id:$
 */
public class WorkspaceRevealService implements RevealService {

    public static void register() {
        RevealServiceFactory.addFactory(Factory.NATIVE_PLATFORM, new RevealServiceFactory() {
            @Override
            protected RevealService create() {
                return new WorkspaceRevealService();
            }
        });
    }

    private static final Object workspace = new Object();

    @Override
    public void reveal(final Local file) {
        synchronized(workspace) {
            // If a second path argument is specified, a new file viewer is opened. If you specify an
            // empty string (@"") for this parameter, the file is selected in the main viewer.
            NSWorkspace.sharedWorkspace().selectFile(file.getAbsolute(), file.getParent().getAbsolute());
        }
    }
}
