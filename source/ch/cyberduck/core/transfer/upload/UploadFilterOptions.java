package ch.cyberduck.core.transfer.upload;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.preferences.PreferencesFactory;

/**
 * @version $Id$
 */
public final class UploadFilterOptions {

    public boolean permissions
            = PreferencesFactory.get().getBoolean("queue.upload.permissions.change");

    public boolean acl
            = PreferencesFactory.get().getBoolean("queue.upload.permissions.change");

    public boolean timestamp
            = PreferencesFactory.get().getBoolean("queue.upload.timestamp.change");

    /**
     * Create temporary filename with an UUID and rename when upload is complete
     */
    public boolean temporary
            = PreferencesFactory.get().getBoolean("queue.upload.file.temporary");

    public UploadFilterOptions() {
        // Defaults
    }

    public UploadFilterOptions(final boolean permissions, final boolean timestamp, final boolean temporary) {
        this.permissions = permissions;
        this.acl = permissions;
        this.timestamp = timestamp;
        this.temporary = temporary;
    }

    public UploadFilterOptions withPermission(boolean enabled) {
        permissions = enabled;
        acl = enabled;
        return this;
    }

    public UploadFilterOptions withTimestamp(boolean enabled) {
        timestamp = enabled;
        return this;
    }

    public UploadFilterOptions withTemporary(boolean enabled) {
        temporary = enabled;
        return this;
    }
}
