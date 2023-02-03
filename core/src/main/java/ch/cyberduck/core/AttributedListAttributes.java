package ch.cyberduck.core;

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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Container for file listing attributes, such as a sorting comparator and filter
 *
 * @see Filter
 */
public class AttributedListAttributes<E> {

    /**
     * The cached version should be superseded
     * with an updated listing.
     */
    private final AtomicBoolean invalid = new AtomicBoolean();

    /**
     * Timestamp this snapshot of a directory listing was taken
     */
    private Long timestamp;

    /**
     * Initialize with default values
     *
     * @see ch.cyberduck.core.NullComparator
     * @see NullFilter
     */
    public AttributedListAttributes() {
    }

    /**
     * Mark cached listing as superseded
     *
     * @param invalid Flag
     */
    public void setInvalid(final boolean invalid) {
        this.invalid.set(invalid);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }

    public AttributedListAttributes<E> withTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * @return true if the listing should be superseded
     */
    public boolean isInvalid() {
        return invalid.get();
    }
}
