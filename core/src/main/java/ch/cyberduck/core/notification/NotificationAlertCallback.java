package ch.cyberduck.core.notification;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.threading.AlertCallback;

public class NotificationAlertCallback implements AlertCallback {

    private NotificationService notification = NotificationServiceFactory.get();

    @Override
    public boolean alert(final Host host, final BackgroundException failure, final StringBuilder transcript) {
        notification.notify(failure.getMessage(), host.getHostname());
        return false;
    }
}
