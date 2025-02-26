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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;

public final class UploadFilterOptions {

    public boolean permissions;
    public boolean acl;
    public boolean timestamp;
    public boolean metadata;
    /**
     * Create temporary filename with an UUID and rename when upload is complete
     */
    public boolean temporary;
    /**
     * Move existing file to versioning directory
     */
    public boolean versioning;
    /**
     * Enable server side encryption if available
     */
    public boolean encryption;
    /**
     * Set storage class
     */
    public boolean redundancy;
    /**
     * Calculate checksum for file
     */
    public boolean checksum;

    public UploadFilterOptions(final Host bookmark) {
        // Defaults
        final PreferencesReader preferences = HostPreferencesFactory.get(bookmark);
        permissions = preferences.getBoolean("queue.upload.permissions.change");
        acl = preferences.getBoolean("queue.upload.acl.change");
        timestamp = preferences.getBoolean("queue.upload.timestamp.change");
        temporary = preferences.getBoolean("queue.upload.file.temporary");
        versioning = preferences.getBoolean("versioning.enable");
        metadata = preferences.getBoolean("queue.upload.file.metadata.change");
        encryption = preferences.getBoolean("queue.upload.file.encryption.change");
        redundancy = preferences.getBoolean("queue.upload.file.redundancy.change");
        checksum = preferences.getBoolean("queue.upload.checksum.calculate");
    }

    public UploadFilterOptions(final boolean permissions, final boolean timestamp, final boolean temporary) {
        this.permissions = permissions;
        this.acl = permissions;
        this.timestamp = timestamp;
        this.temporary = temporary;
    }

    public UploadFilterOptions withPermission(boolean enabled) {
        permissions = enabled;
        return this;
    }

    public UploadFilterOptions withAcl(boolean enabled) {
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

    public UploadFilterOptions withMetadata(boolean enabled) {
        metadata = enabled;
        return this;
    }

    public UploadFilterOptions withEncryption(boolean enabled) {
        encryption = enabled;
        return this;
    }

    public UploadFilterOptions withRedundancy(boolean enabled) {
        redundancy = enabled;
        return this;
    }

    public UploadFilterOptions withChecksum(boolean enabled) {
        checksum = enabled;
        return this;
    }

    public UploadFilterOptions withVersioning(final boolean versioning) {
        this.versioning = versioning;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UploadFilterOptions{");
        sb.append("permissions=").append(permissions);
        sb.append(", acl=").append(acl);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", metadata=").append(metadata);
        sb.append(", temporary=").append(temporary);
        sb.append(", versioning=").append(versioning);
        sb.append(", encryption=").append(encryption);
        sb.append(", redundancy=").append(redundancy);
        sb.append(", checksum=").append(checksum);
        sb.append('}');
        return sb.toString();
    }
}
