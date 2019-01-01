package ch.cyberduck.core.transfer.download;

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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

public final class DownloadFilterOptions {

    /**
     * Split download into segments
     */
    public boolean segments;
    public boolean permissions;
    public boolean timestamp;
    public boolean wherefrom;
    public boolean icon;
    public boolean checksum;

    public DownloadFilterOptions() {
        final Preferences preferences = PreferencesFactory.get();
        segments = preferences.getBoolean("queue.download.segments");
        permissions = preferences.getBoolean("queue.download.permissions.change");
        timestamp = preferences.getBoolean("queue.download.timestamp.change");
        wherefrom = preferences.getBoolean("queue.download.wherefrom");
        icon = preferences.getBoolean("queue.download.icon.update");
        checksum = preferences.getBoolean("queue.download.checksum.calculate");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DownloadFilterOptions{");
        sb.append("segments=").append(segments);
        sb.append(", permissions=").append(permissions);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", wherefrom=").append(wherefrom);
        sb.append(", icon=").append(icon);
        sb.append(", checksum=").append(checksum);
        sb.append('}');
        return sb.toString();
    }
}
