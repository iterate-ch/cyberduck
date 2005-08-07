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
 * @version $Id$
 */
public abstract class BrowserComparator implements Comparator {

    protected boolean ascending;

    public BrowserComparator(boolean ascending) {
        this.ascending = ascending;
    }

    public boolean isAscending() {
        return this.ascending;
    }

    public boolean equals(Object object) {
        if(object instanceof BrowserComparator) {
            BrowserComparator other = (BrowserComparator)object;
            if(other.toString().equals(this.toString())) {
                return other.isAscending() == this.isAscending();
            }
        }
        return false;
    }
}
