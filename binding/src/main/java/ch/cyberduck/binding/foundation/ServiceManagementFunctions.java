package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import com.sun.jna.Library;

public interface ServiceManagementFunctions extends Library {

    /**
     * @param identifier The bundle identifier of the helper application bundle
     * @param enabled    The Boolean enabled state of the helper application. This value is effective only
     *                   for the currently logged in user. If true, the helper application will be started
     *                   immediately (and upon subsequent logins) and kept running. If false, the helper
     *                   application will no longer be kept running.
     * @return Returns true if the requested change has taken effect.
     */
    boolean SMLoginItemSetEnabled(CFStringRef identifier, boolean enabled);
}
