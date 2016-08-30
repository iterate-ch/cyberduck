package ch.cyberduck.core.notification;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

public interface NotificationService {

    /**
     * Register application
     */
    void setup();

    /**
     * Unregister application
     */
    void unregister();

    /**
     * @param title       Non localized title to be looked up in status table
     * @param description Hostname
     */
    void notify(String title, String description);

    /**
     * @param title       Non localized title to be looked up in status table
     * @param description Hostname
     * @param image       Custom icon instead of application icon
     */
    void notifyWithImage(String title, String description, String image);
}