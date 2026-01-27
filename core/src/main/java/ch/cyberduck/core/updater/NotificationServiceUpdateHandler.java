package ch.cyberduck.core.updater;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.notification.NotificationService;
import ch.cyberduck.core.notification.NotificationServiceFactory;
import ch.cyberduck.core.updater.UpdateChecker.Handler;

import java.text.MessageFormat;

public class NotificationServiceUpdateHandler implements Handler, NotificationService.Listener {

    private final NotificationService notifications;
    private final UpdateChecker updater;

    public NotificationServiceUpdateHandler(final UpdateChecker updater) {
        this(NotificationServiceFactory.get(), updater);
    }

    public NotificationServiceUpdateHandler(final NotificationService notifications, final UpdateChecker updater) {
        this.notifications = notifications;
        this.notifications.addListener(this);
        this.updater = updater;
    }

    @Override
    public boolean handle(final UpdateChecker.Update item) {
        notifications.notify(item.getRevision(), "Updater", LocaleFactory.localizedString("Software Update", "Updater"),
                this.toMessage(item),
                String.format("%s…", LocaleFactory.localizedString("Install and Relaunch", "Updater")));
        return false;
    }

    protected String toMessage(final UpdateChecker.Update item) {
        return MessageFormat.format(LocaleFactory.localizedString("Version {0} is now available", "Updater"), item.getDisplayVersionString());
    }

    @Override
    public void callback(final String identifier) {
        // If the notificaton is clicked on, make sure we bring the update in focus
        // If the app is terminated while the notification is clicked on, this will launch the application and perform a new update check
        if("Updater".equals(identifier)) {
            updater.check(false);
        }
    }
}
