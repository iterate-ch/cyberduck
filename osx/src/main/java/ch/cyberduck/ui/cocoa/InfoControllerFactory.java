package ch.cyberduck.ui.cocoa;

import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InfoControllerFactory {

    private static Map<BrowserController, InfoController> open
            = new HashMap<BrowserController, InfoController>();

    private InfoControllerFactory() {
        //
    }

    public static InfoController create(final BrowserController controller, final List<Path> files) {
        if(PreferencesFactory.get().getBoolean("browser.info.inspector")) {
            if(open.containsKey(controller)) {
                final InfoController c = open.get(controller);
                c.setFiles(files);
                return c;
            }
        }
        final InfoController c = new InfoController(controller, files) {
            @Override
            public void windowWillClose(final NSNotification notification) {
                InfoControllerFactory.open.remove(controller);
                super.windowWillClose(notification);
            }
        };
        open.put(controller, c);
        return c;
    }

    /**
     * @param controller Browser
     * @return Null if the browser does not have an Info window.
     */
    public static InfoController get(final BrowserController controller) {
        return open.get(controller);
    }
}
