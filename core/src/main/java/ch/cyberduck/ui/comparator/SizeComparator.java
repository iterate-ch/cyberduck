package ch.cyberduck.ui.comparator;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.Path;

public class SizeComparator extends BrowserComparator {
    private static final long serialVersionUID = -8659327370467434757L;

    public SizeComparator(boolean ascending) {
        super(ascending, new FilenameComparator(ascending));
    }

    @Override
    protected int compareFirst(final Path p1, final Path p2) {
        if(p1.attributes().getSize() > p2.attributes().getSize()) {
            return ascending ? 1 : -1;
        }
        else if(p1.attributes().getSize() < p2.attributes().getSize()) {
            return ascending ? -1 : 1;
        }
        return 0;
    }
}
