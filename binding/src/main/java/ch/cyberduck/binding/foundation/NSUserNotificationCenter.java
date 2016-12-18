package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2012 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import org.rococoa.ObjCClass;

public abstract class NSUserNotificationCenter extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSUserNotificationCenter", _Class.class);

    /**
     * Get a singleton user notification center that posts notifications for this process.<br>
     * Original signature : <code>+(NSUserNotificationCenter*)defaultUserNotificationCenter</code><br>
     * <i>native declaration : line 8</i>
     */
    public static NSUserNotificationCenter defaultUserNotificationCenter() {
        return CLASS.defaultUserNotificationCenter();
    }

    public interface _Class extends ObjCClass {
        /**
         * Get a singleton user notification center that posts notifications for this process.<br>
         * Original signature : <code>+(NSUserNotificationCenter*)defaultUserNotificationCenter</code><br>
         * <i>native declaration : line 8</i>
         */
        public abstract NSUserNotificationCenter defaultUserNotificationCenter();
    }

    // Add a notification to the center for scheduling.
    public abstract void scheduleNotification(NSUserNotification notification);

    // Cancels a notification. If the deliveryDate occurs before the cancellation finishes, the notification
    // may still be delivered. If the notification is not in the scheduled list, nothing happens.
    public abstract void removeScheduledNotification(NSUserNotification notification);

    public abstract void removeAllDeliveredNotifications();
}