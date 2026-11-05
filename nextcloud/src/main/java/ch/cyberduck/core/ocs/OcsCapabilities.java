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
    /**
     * Chunked upload (v2) at dav/uploads
     */
    public boolean chunking;
    /**
     * Maximum size of a chunk in bytes advertised by the server. Zero if unknown.
     */
    public long chunksize;
    /**
     * Maximum number of chunks uploaded in parallel advertised by the server. Zero if unknown.
     */
    public int chunkconcurrency;
    /**
     * Storage spaces with project spaces and share jail
     */
    public boolean spaces;
    public boolean projects;
    public boolean sharejail;

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

    public OcsCapabilities withChunking(final boolean chunking) {
        this.chunking = chunking;
        return this;
    }

    public OcsCapabilities withChunkSize(final long chunksize) {
        this.chunksize = chunksize;
        return this;
    }

    public OcsCapabilities withChunkConcurrency(final int chunkconcurrency) {
        this.chunkconcurrency = chunkconcurrency;
        return this;
    }

    public OcsCapabilities withSpaces(final boolean spaces) {
        this.spaces = spaces;
        return this;
    }

    public OcsCapabilities withProjects(final boolean projects) {
        this.projects = projects;
        return this;
    }

    public OcsCapabilities withSharejail(final boolean sharejail) {
        this.sharejail = sharejail;
        return this;
    }

    @Override
    public String toString() {
        final String sb = "OcsCapabilities{" + "webdav='" + webdav + '\'' +
                ", versioning=" + versioning +
                ", locking=" + locking +
                ", chunking=" + chunking +
                ", chunksize=" + chunksize +
                ", chunkconcurrency=" + chunkconcurrency +
                ", spaces=" + spaces +
                ", projects=" + projects +
                ", sharejail=" + sharejail +
                '}';
        return sb;
    }
}
