package ch.cyberduck.core.notification;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.foundation.NSUserNotification;
import ch.cyberduck.binding.foundation.NSUserNotificationCenter;
import ch.cyberduck.core.LocaleFactory;

import org.rococoa.Foundation;

public class NotificationCenter implements NotificationService {

    private NSUserNotificationCenter center
            = NSUserNotificationCenter.defaultUserNotificationCenter();

    @Override
    public void setup() {
        //
    }

    @Override
    public void unregister() {
        //
    }

    private NSUserNotification create(final String title, final String description) {
        final NSUserNotification notification = NSUserNotification.notification();
        notification.setTitle(LocaleFactory.localizedString(title, "Status"));
        notification.setInformativeText(description);
        if(notification.respondsToSelector(Foundation.selector("setIdentifier:"))) {
            // This identifier is unique to a notification. A notification delivered with the same
            // identifier as an existing notification will replace that notification, rather then display a new one.
            notification.setIdentifier(description);
        }
        return notification;
    }

    @Override
    public void notify(final String title, final String description) {
        final NSUserNotification notification = this.create(title, description);
        center.scheduleNotification(notification);
    }

    @Override
    public void notifyWithImage(final String title, final String description, final String image) {
        final NSUserNotification notification = this.create(title, description);
        notification.setContentImage(NSImage.imageNamed(image));
        center.scheduleNotification(notification);
    }
}