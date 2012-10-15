package ch.cyberduck.core.local;

import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSDistributedNotificationCenter;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;

import org.apache.log4j.Logger;

/**
 * @version $Id$
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
    public void open(final Local file) {
        if(!NSWorkspace.sharedWorkspace().openFile(file.getAbsolute())) {
            log.warn(String.format("Error opening file %s", file.getAbsolute()));
        }
    }

    /**
     * Post a download finished notification to the distributed notification center. Will cause the
     * download folder to bounce just once.
     */
    @Override
    public void bounce(final Local file) {
        NSDistributedNotificationCenter.defaultCenter().postNotification(
                NSNotification.notificationWithName("com.apple.DownloadFileFinished", file.getAbsolute())
        );
    }
}
