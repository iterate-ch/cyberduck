package ch.cyberduck.core.ocs;/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

public final class OcsCapabilities {
    public static final OcsCapabilities none = new OcsCapabilities();

    public String webdav;
    public boolean versioning;
    public boolean locking;

    public OcsCapabilities withWebdav(final String webdav) {
        this.webdav = webdav;
        return this;
    }

    public OcsCapabilities withVersioning(final boolean versioning) {
        this.versioning = versioning;
        return this;
    }

    public OcsCapabilities withLocking(final boolean locking) {
        this.locking = locking;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OcsCapabilities{");
        sb.append("webdav='").append(webdav).append('\'');
        sb.append(", versioning=").append(versioning);
        sb.append(", locking=").append(locking);
        sb.append('}');
        return sb.toString();
    }
}
