package ch.cyberduck.core;

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

import java.util.Comparator;

/**
 * The base class for comparators used to sort by column type in the browser
 * @version $Id$
 */
public abstract class BrowserComparator implements Comparator<Path> {

    protected boolean ascending;

    /**
     *
     * @param ascending The items should be sorted in a ascending manner.
     * Usually this means lower numbers first or natural language sorting
     * for alphabetic comparators
     */
    public BrowserComparator(boolean ascending) {
        this.ascending = ascending;
    }

    public boolean isAscending() {
        return this.ascending;
    }

    /**
     *
     * @param object
     * @see #toString()
     * @see #isAscending()
     * @return True if the same identifier and ascending boolean value
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof BrowserComparator) {
            BrowserComparator other = (BrowserComparator) object;
            if (other.toString().equals(this.toString())) {
                return other.isAscending() == this.isAscending();
            }
        }
        return false;
    }

    public abstract int compare(Path p1, Path p2);

    /**
     * @return An unique identifer for this comparator
     */
    public abstract String toString();
}
