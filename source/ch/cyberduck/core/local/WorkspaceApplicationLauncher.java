package ch.cyberduck.core.local;

import ch.cyberduck.ui.cocoa.application.NSWorkspace;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public final class WorkspaceApplicationLauncher implements ApplicationLauncher {
    private static final Logger log = Logger.getLogger(WorkspaceApplicationLauncher.class);

    public static void register() {
        ApplicationLauncherFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends ApplicationLauncherFactory {
        @Override
        protected ApplicationLauncher create() {
            return new WorkspaceApplicationLauncher();
        }
    }

    private WorkspaceApplicationLauncher() {
        //
    }

    @Override
    public void open(Local file) {
        if(!NSWorkspace.sharedWorkspace().openFile(file.getAbsolute())) {
            log.warn(String.format("Error opening file %s", file.getAbsolute()));
        }
    }
}
