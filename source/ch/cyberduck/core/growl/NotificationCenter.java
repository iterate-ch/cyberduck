package ch.cyberduck.ui.growl;

import ch.cyberduck.ui.cocoa.foundation.NSUserNotification;
import ch.cyberduck.ui.cocoa.foundation.NSUserNotificationCenter;

/**
 * @version $Id$
 */
public class NotificationCenter extends Growl {

    public static void register() {
        if(Factory.VERSION_PLATFORM.matches("10\\.8.*")) {
            GrowlFactory.addFactory(Factory.VERSION_PLATFORM, new Factory());
        }
    }

    @Override
    public void setup() {
        //
    }

    @Override
    public void notify(final String title, final String description) {
        final NSUserNotification notification = NSUserNotification.notification();
        notification.setTitle(title);
        notification.setInformativeText(description);
        NSUserNotificationCenter.defaultUserNotificationCenter().scheduleNotification(notification);
    }

    @Override
    public void notifyWithImage(final String title, final String description, final String image) {
        // No support for custom image. Always use application icon
        this.notify(title, description);
    }

    private static class Factory extends GrowlFactory {
        @Override
        protected Growl create() {
            return new NotificationCenter();
        }
    }
}