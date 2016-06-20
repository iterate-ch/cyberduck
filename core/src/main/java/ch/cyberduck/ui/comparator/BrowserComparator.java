package ch.cyberduck.ui.comparator;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The base class for comparators used to sort by column type in the browser

 */
public abstract class BrowserComparator implements Comparator<Path>, Serializable {
    private static final long serialVersionUID = -5905031111032653689L;

    protected boolean ascending;

    private BrowserComparator fallback;

    /**
     * @param ascending The items should be sorted in a ascending manner.
     *                  Usually this means lower numbers first or natural language sorting
     *                  for alphabetic comparators
     * @param fallback  Second level comparator
     */
    public BrowserComparator(final boolean ascending, final BrowserComparator fallback) {
        this.ascending = ascending;
        this.fallback = fallback;
    }

    public boolean isAscending() {
        return this.ascending;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final BrowserComparator that = (BrowserComparator) o;
        if(ascending != that.ascending) {
            return false;
        }
        if(fallback != null ? !fallback.equals(that.fallback) : that.fallback != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (ascending ? 1 : 0);
        result = 31 * result + (fallback != null ? fallback.hashCode() : 0);
        return result;
    }

    @Override
    public int compare(final Path p1, final Path p2) {
        int result = this.compareFirst(p1, p2);
        if(0 == result) {
            if(null != fallback) {
                return fallback.compareFirst(p1, p2);
            }
        }
        return result;
    }

    protected abstract int compareFirst(final Path p1, final Path p2);
}
