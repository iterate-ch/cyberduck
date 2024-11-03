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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DisabledNotificationService implements NotificationService {
    private static final Logger log = LogManager.getLogger(DisabledNotificationService.class);

    @Override
    public NotificationService setup() {
        return this;
    }

    @Override
    public void unregister() {
        log.warn("Notifications disabled");
    }

    @Override
    public void addListener(final Listener listener) {
        //
    }

    @Override
    public void notify(final String group, final String identifier, final String title, final String description) {
        if(log.isInfoEnabled()) {
            log.info("{}. {}", title, description);
        }
    }

    @Override
    public void notify(final String group, final String identifier, final String title, final String description, final String action) {
        log.warn("Notifications disabled");
    }
}
